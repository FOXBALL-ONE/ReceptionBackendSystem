package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 限流配置。
 *
 * 通过 application.yaml 的 `rate-limit.*` 前缀注入，支持环境变量覆盖（yaml 内带缺省值）。
 * 采用令牌桶算法：[capacity] 为瞬时突发上限，[refillPerMinute] 为持续补充速率（请求/分钟）。
 */
@Component
@ConfigurationProperties(prefix = "rate-limit")
class RateLimitProperties {

    /** 限流总开关。 */
    var enabled: Boolean = true

    /** 令牌桶容量（允许的瞬时突发请求数）。 */
    var capacity: Long = 30

    /** 令牌补充速率（每分钟补充的令牌数，即持续允许的请求/分钟）。 */
    var refillPerMinute: Long = 60
}
