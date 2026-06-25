package top.foxball.receptionbackendsystem.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import top.foxball.receptionbackendsystem.interceptor.LoginInterceptor

/**
 * 登录拦截器注册。
 *
 * 默认拦截所有请求，仅放行对外展示端点（SiteApiController 与站点页面 s 路径）、
 * 登录接口、站点引用的图片资源以及运维端点；其余后台接口与页面均需登录后访问。
 *
 * 注意：SiteApiController 同时映射了带 activityUrl 的子路径与一批 api 顶层路径，
 * 两者都需要在 excludePathPatterns 中显式放行，否则会误拦截对外接口。
 */
@Configuration
class LoginWebConfig(
    private val loginInterceptor: LoginInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                // 站点对外页面（SitePageController）
                "/s",
                "/s/",
                "/s/**",
                // 站点对外接口（SiteApiController）
                "/api/site/**",
                "/api/meta",
                "/api/schedule",
                "/api/people",
                "/api/vehicles",
                "/api/meals",
                "/api/hotel",
                "/api/sites",
                "/api/service",
                "/api/overview",
                // 登录相关接口
                "/api/auth/**",
                // 站点引用的图片资源
                "/images/**",
                // 运维端点与基础资源
                "/error",
                "/favicon.ico",
                "/actuator/**",
            )
    }
}
