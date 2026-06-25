package top.foxball.receptionbackendsystem.service

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 已注销令牌的内存撤销表。
 *
 * JWT 本身是无状态的，退出登录时无法让令牌立即失效；这里用一张内存中的
 * ConcurrentHashMap 记录已注销令牌的 jti，校验时拦截即可实现真正的"登出"。
 * 不依赖 Redis；进程重启后表清空（已登出令牌恢复有效直至自然过期，属可接受的取舍）。
 * 过期条目在访问时惰性清理。
 */
@Component
class JwtTokenStore {
    private val revoked: MutableMap<String, Long> = ConcurrentHashMap()

    /** 标记某令牌为已注销，记录其过期时间便于清理。 */
    fun revoke(jti: String, exp: Long) {
        if (jti.isBlank()) {
            return
        }
        revoked[jti] = exp
        cleanup()
    }

    /** 该令牌是否已被注销。 */
    fun isRevoked(jti: String): Boolean {
        cleanup()
        return jti.isNotBlank() && revoked.containsKey(jti)
    }

    /** 清理已过期的撤销记录，避免内存无限增长。 */
    private fun cleanup() {
        val now = System.currentTimeMillis()
        revoked.entries.removeIf { it.value <= now }
    }
}
