package top.foxball.receptionbackendsystem.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.config.RateLimitProperties
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

/**
 * 基于客户端 IP 的令牌桶限流器。
 *
 * 每个 key（通常是客户端 IP）一个桶：容量 [RateLimitProperties.capacity]、
 * 按速率 [RateLimitProperties.refillPerMinute] 持续补充令牌，每次请求消耗 1 个。
 * 命中返回需等待的秒数（用于 Retry-After），放行返回 null。
 * 内存态（单实例有效）；定期清理空闲桶防止 map 随唯一 IP 数无限增长。
 */
@Service
class RateLimiter(private val props: RateLimitProperties) {

    private class Bucket(var tokens: Double, var lastRefillNanos: Long)

    private val buckets = ConcurrentHashMap<String, Bucket>()

    /**
     * 尝试为 [key] 获取 1 个令牌。
     * @return 被限流时返回需等待的秒数（≥1，用于 Retry-After）；放行返回 null。
     */
    fun tryAcquire(key: String): Long? {
        val now = System.nanoTime()
        val bucket = buckets.computeIfAbsent(key) { Bucket(props.capacity.toDouble(), now) }
        return consume(bucket, now)
    }

    private fun consume(bucket: Bucket, nowNanos: Long): Long? {
        synchronized(bucket) {
            val ratePerNano = props.refillPerMinute.coerceAtLeast(1).toDouble() / 60.0 / NANOS_PER_SECOND
            val elapsed = (nowNanos - bucket.lastRefillNanos).coerceAtLeast(0L)
            bucket.lastRefillNanos = nowNanos
            bucket.tokens = (bucket.tokens + elapsed * ratePerNano).coerceAtMost(props.capacity.toDouble())
            return if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0
                null
            } else {
                val deficit = 1.0 - bucket.tokens
                ceil(deficit / ratePerNano / NANOS_PER_SECOND).toLong().coerceAtLeast(1L)
            }
        }
    }

    /** 每 5 分钟清理超过 10 分钟未活动的桶，防止内存随唯一 IP 数无限增长。 */
    @Scheduled(fixedDelay = SWEEP_INTERVAL_MS)
    fun sweepIdleBuckets() {
        val cutoff = System.nanoTime() - IDLE_THRESHOLD_NS
        val it = buckets.entries.iterator()
        var removed = 0
        while (it.hasNext()) {
            val bucket = it.next().value
            synchronized(bucket) {
                if (bucket.lastRefillNanos < cutoff) {
                    it.remove()
                    removed++
                }
            }
        }
        if (removed > 0) {
            logger.debug("RateLimiter swept {} idle buckets (remaining={})", removed, buckets.size)
        }
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val SWEEP_INTERVAL_MS = 5 * 60_000L
        const val IDLE_THRESHOLD_NS = 10L * 60 * NANOS_PER_SECOND
        private val logger = LoggerFactory.getLogger(RateLimiter::class.java)
    }
}
