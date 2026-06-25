package top.foxball.receptionbackendsystem.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.handlder.UnauthorizedException
import top.foxball.receptionbackendsystem.interceptor.LoginInterceptor
import top.foxball.receptionbackendsystem.service.AccountService
import top.foxball.receptionbackendsystem.shared.AuthTokens
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse

/**
 * 账号自助管理接口。
 *
 * 路径挂在 account 下（不在登录放行清单中），由 LoginInterceptor 校验登录态；
 * 当前登录用户从请求属性中读取。
 */
@RestController
@RequestMapping("/api/account")
class AccountController(
    private val accountService: AccountService,
    private val builder: ResponseBuilder,
) {
    /** 修改密码：需提供原密码与新密码。 */
    @PostMapping("/password")
    fun changePassword(
        @RequestBody request: ChangePasswordRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        accountService.changePassword(userId, request.oldPassword, request.newPassword)
        return builder.ok().message("密码修改成功").build()
    }

    /** 修改用户名：需提供新用户名与当前密码确认；成功返回更新后的令牌与用户信息。 */
    @PostMapping("/username")
    fun changeUsername(
        @RequestBody request: ChangeUsernameRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        val result = accountService.changeUsername(
            userId = userId,
            newUsername = request.newUsername,
            password = request.password,
            currentToken = AuthTokens.extractBearer(httpRequest),
        )
        return builder.ok()
            .message("用户名修改成功")
            .data(LoginResponse(token = result.token, user = result.user.toView()))
            .build()
    }
}

/** 修改密码请求体 */
data class ChangePasswordRequest(
    val oldPassword: String? = null,
    val newPassword: String? = null,
)

/** 修改用户名请求体 */
data class ChangeUsernameRequest(
    val newUsername: String? = null,
    val password: String? = null,
)
