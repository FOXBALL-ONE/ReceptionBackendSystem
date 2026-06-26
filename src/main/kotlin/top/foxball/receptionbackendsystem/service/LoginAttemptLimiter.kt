package top.foxball.receptionbackendsystem.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 登录类失败次数的内存计数器（无 Redis）。
 *
 * 主要用于两步验证动态码/备用码的爆破防护：按挑战令牌 jti 计数，
 * 达到上限后由调用方撤销挑战令牌并要求重新登录。
 * 进程重启后计数清空（可接受的取舍）。
 */
@Service
class LoginAttemptLimiter {
    private val failures: MutableMap<String, AtomicInteger> = ConcurrentHashMap()

    /** 记录一次失败，返回当前累计失败次数。 */
    fun registerFailure(key: String): Int =
        failures.computeIfAbsent(key) { AtomicInteger() }.incrementAndGet()

    /** 成功或锁定后清零。 */
    fun reset(key: String) {
        failures.remove(key)
    }
}
