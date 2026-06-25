package top.foxball.receptionbackendsystem.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository

/**
 * 后台默认管理员初始化器。
 *
 * 因 JPA ddl-auto=create 会在每次启动时重建表，users 表初始为空，
 * 故在应用就绪后按 auth 配置的种子账号写入一条默认管理员（密码以 BCrypt 哈希）。
 * 已存在同名用户时跳过，避免覆盖已有账号。
 */
@Component
class UserDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authProperties: AuthProperties,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun seedDefaultAdmin() {
        if (userRepository.existsByUsername(authProperties.username)) {
            logger.info("Default admin already exists, skip seeding: username={}", authProperties.username)
            return
        }

        val admin = User(
            username = authProperties.username,
            // PasswordEncoder.encode 在严格 jsr305 下被识别为可空返回，但 BCrypt 实现永不返回 null
            password = passwordEncoder.encode(authProperties.password)!!,
            displayName = "管理员",
            enabled = true,
        )
        userRepository.saveAndFlush(admin)
        logger.info("Default admin seeded: username={}", authProperties.username)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(UserDataInitializer::class.java)
    }
}
