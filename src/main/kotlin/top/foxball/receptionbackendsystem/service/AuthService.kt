package top.foxball.receptionbackendsystem.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository
import top.foxball.receptionbackendsystem.handlder.UserDisabledException
import top.foxball.receptionbackendsystem.handlder.UsernameOrPasswordErrorException
import top.foxball.receptionbackendsystem.shared.LoginUser

/**
 * 后台登录服务。
 *
 * 用户信息来源于 users 表，密码以 BCrypt 哈希校验；
 * 登录成功后签发 JWT（由客户端持有并在后续请求的 Authorization 头中携带），
 * 由 LoginInterceptor 校验。退出登录则把令牌加入内存撤销表。
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtTokenStore: JwtTokenStore,
) {
    /**
     * 校验账号密码并签发令牌。
     *
     * 用户不存在与密码错误返回同一异常，避免用户名枚举。
     *
     * @return 令牌与登录用户信息
     * @throws UsernameOrPasswordErrorException 用户名或密码不匹配
     * @throws UserDisabledException 账号被禁用
     */
    fun login(username: String?, password: String?): LoginResult {
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
        val token = jwtService.generate(loginUser)
        return LoginResult(token = token, user = loginUser)
    }

    /**
     * 注销令牌：解析后将其 jti 加入内存撤销表。无令牌或令牌非法时为空操作。
     */
    fun logout(token: String?) {
        val parsed = token?.let { jwtService.parse(it) } ?: return
        jwtTokenStore.revoke(parsed.jti, parsed.exp)
    }

    /**
     * 根据令牌解析当前登录用户；令牌缺失、非法、过期或已注销时返回 null。
     */
    fun currentUserFromToken(token: String?): LoginUser? {
        val parsed = token?.let { jwtService.parse(it) } ?: return null
        if (jwtTokenStore.isRevoked(parsed.jti)) {
            return null
        }
        return parsed.user
    }

}

/** 将 User 实体投影为登录态用户信息（供 AuthService / AccountService 复用）。 */
fun User.toLoginUser(): LoginUser =
    LoginUser(
        id = id,
        username = username,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: username,
    )

/** 登录结果。 */
data class LoginResult(
    val token: String,
    val user: LoginUser,
)
