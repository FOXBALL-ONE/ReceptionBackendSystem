package top.foxball.receptionbackendsystem.shared

import jakarta.servlet.http.HttpServletRequest

/**
 * Bearer 令牌解析工具。
 */
object AuthTokens {
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER_PREFIX = "Bearer "

    /**
     * 从请求头中解析 Bearer 令牌；缺失或格式不符时返回 null。
     */
    fun extractBearer(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!header.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            return null
        }
        return header.removePrefix(BEARER_PREFIX).trim().takeIf { it.isNotEmpty() }
    }
}
