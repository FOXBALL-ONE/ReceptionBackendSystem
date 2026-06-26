package top.foxball.receptionbackendsystem.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.handlder.UserDisabledException
import top.foxball.receptionbackendsystem.handlder.UsernameOrPasswordErrorException
import top.foxball.receptionbackendsystem.shared.LoginUser

/**
 * 登录与会话服务。
 *
 * 密码校验始终以 users 表为准；登录态由 JWT 承载。
 * 开启了 TOTP 两步验证的用户，密码正确后先返回挑战令牌，需再校验动态码/备用码才签发登录令牌。
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtTokenStore: JwtTokenStore,
    private val totpService: TotpService,
    private val secretCrypto: SecretCrypto,
    private val loginAttemptLimiter: LoginAttemptLimiter,
) {
    /**
     * 第一步登录：校验账号密码。
     * - 未开启两步验证：直接签发登录令牌。
     * - 已开启两步验证：签发短时挑战令牌，前端再调 verifyTotpLogin / verifyBackupLogin。
     *
     * 用户不存在与密码错误返回同一异常，避免用户名枚举。
     */
    fun login(username: String?, password: String?): LoginOutcome {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            throw UsernameOrPasswordErrorException()
        }
        val user = userRepository.findByUsername(username)
            ?: throw UsernameOrPasswordErrorException()
        if (!user.enabled) {
            throw UserDisabledException()
        }
        if (!passwordEncoder.matches(password, user.password)) {
            throw UsernameOrPasswordErrorException()
        }

        val loginUser = user.toLoginUser()
        return if (user.totpEnabled == true) {
            LoginOutcome.TwoFactorRequired(
                challengeToken = jwtService.generate(loginUser, JwtService.TokenType.TFA_CHALLENGE),
            )
        } else {
            LoginOutcome.LoggedIn(
                token = jwtService.generate(loginUser),
                user = loginUser,
            )
        }
    }

    /** 第二步：校验 TOTP 动态码，成功后消耗挑战令牌并签发登录令牌。 */
    @Transactional
    fun verifyTotpLogin(challengeToken: String?, code: String?): LoginResult {
        val parsed = parseChallenge(challengeToken)
        val user = loadUserById(parsed.user.id)

        if (user.totpEnabled != true || user.totpSecret == null) {
            throw ParamErrorException("未开启两步验证")
        }
        val plainSecret = secretCrypto.decrypt(user.totpSecret!!)
        if (code.isNullOrBlank() || !totpService.verify(plainSecret, code.trim())) {
            onTotpFailure(parsed, "动态码错误")
        }

        val step = System.currentTimeMillis() / TOTP_STEP_MS
        if (user.totpLastUsed != null && step <= user.totpLastUsed!!) {
            throw ParamErrorException("动态码已使用，请等待下一个周期")
        }

        consumeChallenge(parsed)
        user.totpLastUsed = step
        userRepository.save(user)

        val loginUser = user.toLoginUser()
        return LoginResult(token = jwtService.generate(loginUser), user = loginUser)
    }

    /** 第二步：使用备用码登录（命中即消耗一枚）。 */
    @Transactional
    fun verifyBackupLogin(challengeToken: String?, backupCode: String?): LoginResult {
        val parsed = parseChallenge(challengeToken)
        val user = loadUserById(parsed.user.id)

        if (user.totpEnabled != true) {
            throw ParamErrorException("未开启两步验证")
        }
        val code = backupCode?.trim()?.takeIf { it.isNotBlank() }
            ?: throw ParamErrorException("请输入备用码")

        val hashes = user.totpBackupCodes
            ?.split("\n")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val matchedIndex = hashes.indexOfFirst { passwordEncoder.matches(code, it) }
        if (matchedIndex < 0) {
            onTotpFailure(parsed, "备用码错误")
        }

        val remaining = hashes.toMutableList().apply { removeAt(matchedIndex) }
        user.totpBackupCodes = remaining.takeIf { it.isNotEmpty() }?.joinToString("\n")
        consumeChallenge(parsed)
        userRepository.save(user)

        val loginUser = user.toLoginUser()
        return LoginResult(token = jwtService.generate(loginUser), user = loginUser)
    }

    /** 注销令牌：解析后将其 jti 加入内存撤销表。无令牌或令牌非法时为空操作。 */
    fun logout(token: String?) {
        val parsed = token?.let { jwtService.parse(it) } ?: return
        jwtTokenStore.revoke(parsed.jti, parsed.exp)
    }

    /** 根据令牌解析当前登录用户；令牌缺失、非法、过期或已注销时返回 null。 */
    fun currentUserFromToken(token: String?): LoginUser? {
        val parsed = token?.let { jwtService.parse(it) } ?: return null
        if (parsed.type != JwtService.TokenType.LOGIN) {
            return null
        }
        if (jwtTokenStore.isRevoked(parsed.jti)) {
            return null
        }
        return parsed.user
    }

    private fun parseChallenge(token: String?): JwtService.ParsedJwt {
        if (token.isNullOrBlank()) {
            throw ParamErrorException("缺少两步验证凭证")
        }
        val parsed = jwtService.parse(token)
            ?: throw ParamErrorException("两步验证凭证无效或已过期")
        if (parsed.type != JwtService.TokenType.TFA_CHALLENGE) {
            throw ParamErrorException("两步验证凭证无效")
        }
        if (jwtTokenStore.isRevoked(parsed.jti)) {
            throw ParamErrorException("两步验证凭证已使用，请重新登录")
        }
        return parsed
    }

    private fun loadUserById(id: Long?): User {
        if (id == null) {
            throw ParamErrorException("两步验证凭证无效")
        }
        return userRepository.findById(id).orElse(null)
            ?: throw ResourceNotFoundException("用户不存在")
    }

    /** 两步验证失败：累计计数，达到上限则撤销挑战令牌并强制重新登录。 */
    private fun onTotpFailure(parsed: JwtService.ParsedJwt, message: String): Nothing {
        val fails = loginAttemptLimiter.registerFailure(parsed.jti)
        if (fails >= MAX_TOTP_ATTEMPTS) {
            jwtTokenStore.revoke(parsed.jti, parsed.exp)
            loginAttemptLimiter.reset(parsed.jti)
            throw ParamErrorException("验证错误次数过多，请重新登录")
        }
        throw ParamErrorException(message)
    }

    private fun consumeChallenge(parsed: JwtService.ParsedJwt) {
        jwtTokenStore.revoke(parsed.jti, parsed.exp)
        loginAttemptLimiter.reset(parsed.jti)
    }

    private companion object {
        private const val MAX_TOTP_ATTEMPTS = 5
        private const val TOTP_STEP_MS = 30_000L
    }
}

/** 登录第一步的结果：直接登录成功，或需要两步验证。 */
sealed interface LoginOutcome {
    data class LoggedIn(val token: String, val user: LoginUser) : LoginOutcome
    data class TwoFactorRequired(val challengeToken: String) : LoginOutcome
}

/** 完成登录（或切换登录态）后的令牌与用户信息。 */
data class LoginResult(
    val token: String,
    val user: LoginUser,
)

/** 将 User 实体投影为登录态用户信息（供 AuthService / AccountService 复用）。 */
fun User.toLoginUser(): LoginUser =
    LoginUser(
        id = id,
        username = username,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: username,
    )
