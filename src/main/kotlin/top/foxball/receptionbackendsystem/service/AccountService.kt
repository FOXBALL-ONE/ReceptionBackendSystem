package top.foxball.receptionbackendsystem.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.handlder.UserAlreadyExistsException

/**
 * 账号自助管理服务：修改密码、修改用户名。
 *
 * 两个接口都需要当前用户已登录（由 LoginInterceptor 校验并把 LoginUser 放入请求属性），
 * 并要求二次确认身份（原密码 / 当前密码），避免会话被劫持后恶意修改。
 */
@Service
class AccountService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtTokenStore: JwtTokenStore,
) {
    /**
     * 修改密码：校验原密码后用 BCrypt 重新哈希写入。当前会话令牌保持有效。
     *
     * @throws ParamErrorException 原密码错误或新密码不合规（一律 400，避免触发前端 401 登出）
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
     *
     * 用户名变更会使旧令牌中的 username 声明失效，因此撤销旧令牌并签发新令牌返回，
     * 由前端替换本地令牌以保持一致。
     *
     * @throws ParamErrorException 密码错误或新用户名不合规
     * @throws UserAlreadyExistsException 新用户名已被占用
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

    private fun loadUser(userId: Long): User =
        userRepository.findById(userId).orElse(null)
            ?: throw ResourceNotFoundException("用户不存在")

    private companion object {
        private const val PASSWORD_MIN_LENGTH = 6
        private const val USERNAME_MAX_LENGTH = 64
    }
}
