package top.foxball.receptionbackendsystem.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security 配置。
 *
 * 登录认证由自定义的 LoginInterceptor（见 LoginWebConfig）基于 HttpSession 实现，
 * 因此这里继续保持 Security 默认认证入口关闭并放行全部请求，避免与拦截器叠加鉴权。
 * 需要登录的后台接口与页面由拦截器统一校验。
 */
@Configuration
class SecurityConfig {

    /**
     * 放行所有请求，并关闭默认认证入口。
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .build()
    }

    /**
     * 密码编码器，users 表中的密码以 BCrypt 哈希存储与校验。
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
