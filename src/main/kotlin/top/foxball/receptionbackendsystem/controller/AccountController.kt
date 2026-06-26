package top.foxball.receptionbackendsystem.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
            .data(LoginResponse(twoFactorRequired = false, token = result.token, user = result.user.toView()))
            .build()
    }

    /** 查询两步验证状态。 */
    @GetMapping("/totp/status")
    fun totpStatus(httpRequest: HttpServletRequest): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        val status = accountService.totpStatus(userId)
        return builder.ok().data(
            TotpStatusResponse(
                enabled = status.enabled,
                pending = status.pending,
                backupRemaining = status.backupRemaining,
                backupUsed = status.backupUsed,
            )
        ).build()
    }

    /** 发起两步验证设置：返回明文密钥与 otpauth URI。 */
    @PostMapping("/totp/setup")
    fun totpSetup(httpRequest: HttpServletRequest): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        val view = accountService.totpSetup(userId)
        return builder.ok().data(TotpSetupResponse(secret = view.secret, otpauthUri = view.otpauthUri)).build()
    }

    /** 确认启用两步验证：校验动态码，成功返回一次性备用码。 */
    @PostMapping("/totp/enable")
    fun totpEnable(
        @RequestBody request: TotpEnableRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        val backupCodes = accountService.totpEnable(userId, request.code)
        return builder.ok().message("两步验证已开启").data(TotpEnableResponse(backupCodes = backupCodes)).build()
    }

    /** 重置备用码：需当前密码确认；生成新的 10 枚备用码，旧备用码全部失效，明文仅此一次返回。 */
    @PostMapping("/totp/backup/reset")
    fun totpResetBackup(
        @RequestBody request: TotpResetBackupRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        val backupCodes = accountService.totpResetBackupCodes(userId, request.password)
        return builder.ok().message("备用码已重置").data(TotpEnableResponse(backupCodes = backupCodes)).build()
    }

    /** 关闭两步验证：需当前密码确认。 */
    @PostMapping("/totp/disable")
    fun totpDisable(
        @RequestBody request: TotpDisableRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = LoginInterceptor.currentLoginUser(httpRequest)?.id
            ?: throw UnauthorizedException()
        accountService.totpDisable(userId, request.password)
        return builder.ok().message("两步验证已关闭").build()
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

/** 两步验证状态响应 */
data class TotpStatusResponse(
    val enabled: Boolean,
    val pending: Boolean,
    val backupRemaining: Int,
    val backupUsed: Int,
)

/** 两步验证设置响应：明文密钥与 otpauth URI */
data class TotpSetupResponse(
    val secret: String,
    val otpauthUri: String,
)

/** 确认启用两步验证请求体 */
data class TotpEnableRequest(
    val code: String? = null,
)

/** 确认启用两步验证响应：一次性备用码 */
data class TotpEnableResponse(
    val backupCodes: List<String>,
)

/** 关闭两步验证请求体 */
data class TotpDisableRequest(
    val password: String? = null,
)

/** 重置备用码请求体 */
data class TotpResetBackupRequest(
    val password: String? = null,
)
