package top.foxball.receptionbackendsystem.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import top.foxball.receptionbackendsystem.config.RateLimitProperties
import top.foxball.receptionbackendsystem.service.RateLimiter

/**
 * 基于客户端 IP 的限流拦截器（令牌桶）。
 *
 * 命中限流时直接写出 429 响应并附带 Retry-After 头。拦截器抛出的异常不会被
 * @RestControllerAdvice 捕获，故仿 LoginInterceptor 直接写响应体。
 *
 * IP 提取：优先取 X-Forwarded-For 首段（反代场景的真实客户端），回退 remoteAddr。
 * 注意：前置反代必须覆盖/清洗客户端自带的 X-Forwarded-For，否则 IP 可被伪造绕过限流。
 */
@Component
class RateLimitInterceptor(
    private val rateLimiter: RateLimiter,
    private val properties: RateLimitProperties,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!properties.enabled) return true
        val retryAfter = rateLimiter.tryAcquire(clientIp(request)) ?: return true
        response.status = TOO_MANY_REQUESTS
        response.setHeader("Retry-After", retryAfter.toString())
        response.contentType = RESPONSE_CONTENT_TYPE
        response.writer.write(TOO_MANY_REQUESTS_BODY)
        return false
    }

    /** 客户端 IP：优先取代理链首段，回退 remoteAddr。 */
    private fun clientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")
            ?.substringBefore(',')
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: request.remoteAddr

    private companion object {
        const val TOO_MANY_REQUESTS = 429
        const val RESPONSE_CONTENT_TYPE = "application/json;charset=UTF-8"
        const val TOO_MANY_REQUESTS_BODY = """{"status":429,"message":"请求过于频繁，请稍后再试","data":{}}"""
    }
}
