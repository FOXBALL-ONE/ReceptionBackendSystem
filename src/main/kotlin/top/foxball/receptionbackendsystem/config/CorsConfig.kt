package top.foxball.receptionbackendsystem.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 跨域（CORS）配置。
 *
 * 放行来源由环境变量 `CORS_ALLOWED_ORIGINS`（逗号分隔，支持半角/全角逗号）控制：
 * - 未设置/为空（DEV 模式）：放宽，允许任意来源（`allowedOriginPatterns("*")`）；
 * - 已设置（生产模式）：仅允许配置的具体来源（`allowedOrigins`），收敛跨域暴露面。
 *
 * 凭据携带（allowCredentials）始终开启；故通配来源必须用 `allowedOriginPatterns` 而非
 * `allowedOrigins`，否则 Spring 会拒绝 "allowedOrigins=* 与 allowCredentials=true" 的非法组合。
 * 若 prod profile 下仍未配置来源（疑似漏配），启动时打 WARN 提醒。
 */
@Configuration
class CorsConfig(
    @Value("\${CORS_ALLOWED_ORIGINS:}")
    private val allowedOriginsConfig: String,
    private val environment: Environment,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        val mapping = registry.addMapping("/**")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)

        val origins = allowedOriginsConfig.split(',', '，')
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        if (origins.isEmpty()) {
            // DEV：未配置具体来源 → 放宽为任意来源
            if (environment.acceptsProfiles(Profiles.of("prod"))) {
                logger.warn("CORS_ALLOWED_ORIGINS 未配置但当前为 prod profile，CORS 将保持放宽（任意来源）——请尽快配置生产前端来源！")
            }
            mapping.allowedOriginPatterns("*")
        } else {
            // 生产：仅放行配置的具体来源
            mapping.allowedOrigins(*origins.toTypedArray())
        }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(CorsConfig::class.java)
    }
}
