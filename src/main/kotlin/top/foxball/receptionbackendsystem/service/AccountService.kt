package top.foxball.receptionbackendsystem.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.handlder.UserAlreadyExistsException
import java.security.SecureRandom

/**
 * 账号自助管理服务：修改密码、修改用户名，以及 TOTP 两步验证的开启/关闭。
 *
 * 所有方法都需要当前用户已登录（由 LoginInterceptor 校验），并要求二次确认身份
 * （原密码 / 当前密码 / 动态码），避免会话被劫持后恶意修改。
 */
@Service
class AccountService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtTokenStore: JwtTokenStore,
    private val totpService: TotpService,
    private val secretCrypto: SecretCrypto,
) {
    /**
     * 修改密码：校验原密码后用 BCrypt 重新哈希写入。当前会话令牌保持有效。
     */
    @Transactional
    fun changePassword(userId: Long, oldPassword: String?, newPassword: String?) {
        val user = loadUser(userId)

        val oldPw = oldPassword ?: throw ParamErrorException("请输入原密码")
        val newPw = newPassword ?: throw ParamErrorException("请输入新密码")

        if (!passwordEncoder.matches(oldPw, user.password)) {
            throw ParamErrorException("原密码错误")
        }
        if (newPw.length < PASSWORD_MIN_LENGTH) {
            throw ParamErrorException("新密码长度不能少于 $PASSWORD_MIN_LENGTH 位")
        }
        if (newPw == oldPw) {
            throw ParamErrorException("新密码不能与原密码相同")
        }

        user.password = passwordEncoder.encode(newPw)!!
        userRepository.save(user)
    }

    /**
     * 修改用户名：校验当前密码、校验唯一性后更新。
     * 用户名变更会使旧令牌声明失效，因此撤销旧令牌并签发新令牌返回。
     */
    @Transactional
    fun changeUsername(
        userId: Long,
        newUsername: String?,
        password: String?,
        currentToken: String?,
    ): LoginResult {
        val user = loadUser(userId)

        val pw = password ?: throw ParamErrorException("请输入密码以确认身份")
        if (!passwordEncoder.matches(pw, user.password)) {
            throw ParamErrorException("密码错误")
        }

        val trimmed = newUsername?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.length > USERNAME_MAX_LENGTH) {
            throw ParamErrorException("用户名长度需在 1~$USERNAME_MAX_LENGTH 之间")
        }
        if (trimmed == user.username) {
            throw ParamErrorException("新用户名与当前相同")
        }
        if (userRepository.existsByUsername(trimmed)) {
            throw UserAlreadyExistsException("用户名已被占用")
        }

        user.username = trimmed
        userRepository.save(user)

        currentToken?.let { jwtService.parse(it) }?.let { jwtTokenStore.revoke(it.jti, it.exp) }
        val loginUser = user.toLoginUser()
        val token = jwtService.generate(loginUser)
        return LoginResult(token = token, user = loginUser)
    }

    /**
     * 两步验证状态：enabled=已启用；pending=已生成密钥但尚未确认启用。
     */
    fun totpStatus(userId: Long): TotpStatus {
        val user = loadUser(userId)
        return TotpStatus(
            enabled = user.totpEnabled == true,
            pending = user.totpEnabled != true && user.totpSecret != null,
        )
    }

    /**
     * 发起两步验证设置：生成密钥（加密入库、enabled=false），返回明文密钥与 otpauth URI 供前端画二维码。
     * 已启用时需先关闭。
     */
    @Transactional
    fun totpSetup(userId: Long): TotpSetupView {
        val user = loadUser(userId)
        if (user.totpEnabled == true) {
            throw ParamErrorException("请先关闭当前两步验证")
        }
        val secret = totpService.generateSecret()
        user.totpSecret = secretCrypto.encrypt(secret)
        user.totpEnabled = false
        userRepository.save(user)
        return TotpSetupView(secret = secret, otpauthUri = totpService.otpauthUri(secret, user.username))
    }

    /**
     * 确认启用两步验证：用动态码校验待确认密钥，通过后置 enabled=true 并生成备用码。
     * 备用码明文仅此一次返回。
     */
    @Transactional
    fun totpEnable(userId: Long, code: String?): List<String> {
        val user = loadUser(userId)
        if (user.totpEnabled == true) {
            throw ParamErrorException("两步验证已开启")
        }
        val encrypted = user.totpSecret
            ?: throw ParamErrorException("请先获取两步验证二维码")
        if (code.isNullOrBlank() || !totpService.verify(secretCrypto.decrypt(encrypted), code.trim())) {
            throw ParamErrorException("动态码错误")
        }

        user.totpEnabled = true
        user.totpLastUsed = null
        val codes = generateBackupCodes(BACKUP_CODE_COUNT)
        user.totpBackupCodes = codes.joinToString("\n") { passwordEncoder.encode(it)!! }
        userRepository.save(user)
        return codes
    }

    /**
     * 关闭两步验证：需当前密码确认，清空密钥、备用码与防重放记录。
     */
    @Transactional
    fun totpDisable(userId: Long, password: String?) {
        val user = loadUser(userId)
        if (!passwordEncoder.matches(password ?: "", user.password)) {
            throw ParamErrorException("密码错误")
        }
        user.totpSecret = null
        user.totpEnabled = false
        user.totpBackupCodes = null
        user.totpLastUsed = null
        userRepository.save(user)
    }

    private fun loadUser(userId: Long): User =
        userRepository.findById(userId).orElse(null)
            ?: throw ResourceNotFoundException("用户不存在")

    /** 生成 count 个格式为 XXXX-XXXX 的十六进制备用码。 */
    private fun generateBackupCodes(count: Int): List<String> =
        (1..count).map {
            val hex = ByteArray(BACKUP_CODE_BYTES).also { SECURE_RANDOM.nextBytes(it) }
                .joinToString("") { byte -> "%02X".format(byte) }
            "${hex.take(4)}-${hex.drop(4)}"
        }

    private companion object {
        private const val PASSWORD_MIN_LENGTH = 6
        private const val USERNAME_MAX_LENGTH = 64
        private const val BACKUP_CODE_COUNT = 10
        private const val BACKUP_CODE_BYTES = 4
        private val SECURE_RANDOM = SecureRandom()
    }
}

/** 两步验证状态。 */
data class TotpStatus(
    val enabled: Boolean,
    val pending: Boolean,
)

/** 两步验证设置视图：明文密钥与 otpauth URI（前端画二维码）。 */
data class TotpSetupView(
    val secret: String,
    val otpauthUri: String,
)
