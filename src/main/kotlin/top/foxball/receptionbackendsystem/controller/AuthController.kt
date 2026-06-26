package top.foxball.receptionbackendsystem.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.service.AuthService
import top.foxball.receptionbackendsystem.service.LoginOutcome
import top.foxball.receptionbackendsystem.shared.AuthTokens
import top.foxball.receptionbackendsystem.shared.LoginUser
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse

/**
 * 后台登录接口。
 *
 * 该控制器下的路径在 LoginWebConfig 中被放行，无需登录即可访问。
 * login/logout/current 由控制器自解析令牌；login/totp 与 login/backup 完成两步登录。
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val builder: ResponseBuilder,
) {
    /** 第一步登录。未开启两步验证直接返回令牌；已开启返回 challengeToken。 */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse> {
        val outcome = authService.login(request.username, request.password)
        val body = when (outcome) {
            is LoginOutcome.LoggedIn -> LoginResponse(
                twoFactorRequired = false,
                token = outcome.token,
                user = outcome.user.toView(),
            )
            is LoginOutcome.TwoFactorRequired -> LoginResponse(
                twoFactorRequired = true,
                challengeToken = outcome.challengeToken,
            )
        }
        val message = if (outcome is LoginOutcome.TwoFactorRequired) "需要两步验证" else "登录成功"
        return builder.ok().message(message).data(body).build()
    }

    /** 第二步：用动态码完成登录。 */
    @PostMapping("/login/totp")
    fun loginTotp(@RequestBody request: TotpLoginRequest): ResponseEntity<ApiResponse> {
        val result = authService.verifyTotpLogin(request.challengeToken, request.code)
        return builder.ok()
            .message("登录成功")
            .data(LoginResponse(twoFactorRequired = false, token = result.token, user = result.user.toView()))
            .build()
    }

    /** 第二步：用备用码完成登录。 */
    @PostMapping("/login/backup")
    fun loginBackup(@RequestBody request: BackupLoginRequest): ResponseEntity<ApiResponse> {
        val result = authService.verifyBackupLogin(request.challengeToken, request.backupCode)
        return builder.ok()
            .message("登录成功")
            .data(LoginResponse(twoFactorRequired = false, token = result.token, user = result.user.toView()))
            .build()
    }

    /** 注销令牌：将当前令牌加入内存撤销表。 */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<ApiResponse> {
        authService.logout(AuthTokens.extractBearer(request))
        return builder.ok().message("已退出登录").build()
    }

    /** 查询当前登录用户，未登录或令牌无效时 data 为 null。 */
    @GetMapping("/current")
    fun current(request: HttpServletRequest): ResponseEntity<ApiResponse> {
        val user = authService.currentUserFromToken(AuthTokens.extractBearer(request))
        return builder.ok().data(user?.toView()).build()
    }
}

/** 登录请求体 */
data class LoginRequest(
    val username: String? = null,
    val password: String? = null,
)

/** 两步登录：动态码请求体 */
data class TotpLoginRequest(
    val challengeToken: String? = null,
    val code: String? = null,
)

/** 两步登录：备用码请求体 */
data class BackupLoginRequest(
    val challengeToken: String? = null,
    val backupCode: String? = null,
)

/**
 * 登录响应（统一结构，前端按 twoFactorRequired 分流）。
 * - 普通登录 / 两步登录成功：twoFactorRequired=false，带 token 与 user。
 * - 需要两步验证：twoFactorRequired=true，带 challengeToken。
 */
data class LoginResponse(
    val twoFactorRequired: Boolean = false,
    val token: String? = null,
    val user: LoginUserView? = null,
    val challengeToken: String? = null,
)

/** 登录用户视图，回显给前端 */
data class LoginUserView(
    val id: Long?,
    val username: String,
    val displayName: String?,
    val loginAt: Long,
)

/** 登录用户视图转换，供 AuthController / AccountController 复用。 */
fun LoginUser.toView(): LoginUserView =
    LoginUserView(
        id = id,
        username = username,
        displayName = displayName,
        loginAt = loginAt,
    )
