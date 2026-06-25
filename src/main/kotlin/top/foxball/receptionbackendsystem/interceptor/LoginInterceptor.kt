package top.foxball.receptionbackendsystem.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import top.foxball.receptionbackendsystem.service.JwtService
import top.foxball.receptionbackendsystem.service.JwtTokenStore
import top.foxball.receptionbackendsystem.shared.AuthTokens
import top.foxball.receptionbackendsystem.shared.LoginUser

/**
 * 登录校验拦截器。
 *
 * 从 Authorization: Bearer 头中取出 JWT，校验签名、有效期并排除已注销令牌；
 * 通过后将登录用户存入请求属性供后续使用，未通过则写出统一的 401 JSON 响应。
 */
@Component
class LoginInterceptor(
    private val jwtService: JwtService,
    private val jwtTokenStore: JwtTokenStore,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token = AuthTokens.extractBearer(request)
        val parsed = token?.let { jwtService.parse(it) }

        if (parsed == null || jwtTokenStore.isRevoked(parsed.jti)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = RESPONSE_CONTENT_TYPE
            response.writer.write(UNAUTHORIZED_BODY)
            return false
        }

        request.setAttribute(ATTR_LOGIN_USER, parsed.user)
        return true
    }

    companion object {
        const val ATTR_LOGIN_USER = "loginUser"

        /** 从请求属性中读取已登录用户（仅受保护接口可用）。 */
        fun currentLoginUser(request: HttpServletRequest): LoginUser? =
            request.getAttribute(ATTR_LOGIN_USER) as? LoginUser

        private const val RESPONSE_CONTENT_TYPE = "application/json;charset=UTF-8"
        private const val UNAUTHORIZED_BODY = """{"status":401,"message":"未登录或登录已过期","data":{}}"""
    }
}
