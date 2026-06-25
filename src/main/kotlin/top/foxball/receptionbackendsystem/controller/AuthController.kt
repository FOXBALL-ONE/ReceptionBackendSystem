package top.foxball.receptionbackendsystem.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.service.AuthService
import top.foxball.receptionbackendsystem.shared.AuthTokens
import top.foxball.receptionbackendsystem.shared.LoginUser
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse

/**
 * 后台登录接口。
 *
 * 该控制器下的路径在 LoginWebConfig 中被放行，无需登录即可访问，
 * 用于完成登录、登出以及查询当前登录态。登出与查询当前用户由控制器自行解析令牌。
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val builder: ResponseBuilder,
) {
    /** 管理员登录，成功后返回 JWT 令牌与用户信息。 */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse> {
        val result = authService.login(request.username, request.password)
        return builder.ok()
            .message("登录成功")
            .data(LoginResponse(token = result.token, user = result.user.toView()))
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

/** 登录用户视图转换，供 AuthController / AccountController 复用。 */
fun LoginUser.toView(): LoginUserView =
    LoginUserView(
        id = id,
        username = username,
        displayName = displayName,
        loginAt = loginAt,
    )

/** 登录请求体 */
data class LoginRequest(
    val username: String? = null,
    val password: String? = null,
)

/** 登录响应：令牌与用户信息 */
data class LoginResponse(
    val token: String,
    val user: LoginUserView,
)

/** 登录用户视图，回显给前端 */
data class LoginUserView(
    val id: Long?,
    val username: String,
    val displayName: String?,
    val loginAt: Long,
)
