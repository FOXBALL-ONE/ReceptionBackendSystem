package top.foxball.receptionbackendsystem.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.config.AuthProperties
import top.foxball.receptionbackendsystem.config.ForceInitMode
import top.foxball.receptionbackendsystem.config.SystemBootstrapProperties
import top.foxball.receptionbackendsystem.datasource.jdbc.SystemState
import top.foxball.receptionbackendsystem.datasource.jdbc.SystemStateRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.User
import top.foxball.receptionbackendsystem.datasource.jdbc.UserRepository
import java.time.LocalDateTime

/**
 * 系统初始化（bootstrap）服务。
 *
 * 应用就绪后判断并执行初始化：
 * - 未初始化（system_state 无行或 initialized=false）→ 执行初始化步骤并置位；
 * - 已初始化且非强制 → 跳过；
 * - 强制（SYSTEM_FORCE_INIT=true / admin-reset）→ 重跑幂等步骤并刷新审计，
 *   admin-reset 模式额外按 auth.* 重置管理员口令。
 *
 * 初始管理员的账号/口令取自 auth.username / auth.password（环境变量
 * AUTH_USERNAME / AUTH_PASSWORD 未设置时默认 admin / admin123）。
 * 本服务收敛了原 UserDataInitializer 的管理员播种逻辑。
 */
@Service
class SystemBootstrapService(
    private val stateRepository: SystemStateRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authProperties: AuthProperties,
    private val bootstrapProperties: SystemBootstrapProperties,
    private val buildProperties: ObjectProvider<BuildProperties>,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun bootstrap() {
        val state = stateRepository.findById(SystemState.SINGLETON_ID).orElseGet { SystemState() }
        val mode = bootstrapProperties.forceMode

        if (state.initialized && mode == ForceInitMode.NONE) {
            logger.info("System already initialized at {}, skip bootstrap", state.initializedAt)
            return
        }

        ensureAdmin(mode)

        val now = LocalDateTime.now()
        state.initialized = true
        state.initializedAt = now
        state.initializedBy = bootstrapProperties.auditActor
        state.schemaVersion = buildProperties.ifAvailable?.version
        if (mode != ForceInitMode.NONE) {
            state.lastForceInitAt = now
            state.lastForceInitBy = bootstrapProperties.auditActor
        }
        stateRepository.save(state)

        if (mode != ForceInitMode.NONE) {
            logger.warn("SYSTEM_FORCE_INIT={} consumed; please remove it from .env after verification", mode)
        } else {
            logger.info("System bootstrap completed (first initialization)")
        }
    }

    /**
     * 确保存在按 auth.* 配置的管理员（幂等）。
     * - ADMIN_RESET：按 auth.username 重置口令为 auth.password（口令源受 prod fail-fast 保护）；
     * - 其它（首次初始化或 RESEED）：仅在用户名不存在时播种，已存在则不动。
     */
    private fun ensureAdmin(mode: ForceInitMode) {
        val username = authProperties.username
        val existing = userRepository.findByUsername(username)

        if (existing != null) {
            if (mode == ForceInitMode.ADMIN_RESET) {
                existing.password = passwordEncoder.encode(authProperties.password)!!
                userRepository.saveAndFlush(existing)
                logger.warn("Admin password reset by force-init: username={}", username)
            }
            return
        }

        val admin = User(
            username = username,
            // PasswordEncoder.encode 在严格 jsr305 下被识别为可空返回，但 BCrypt 实现永不返回 null
            password = passwordEncoder.encode(authProperties.password)!!,
            displayName = "管理员",
            enabled = true,
        )
        userRepository.saveAndFlush(admin)
        logger.info("Default admin seeded: username={}", username)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(SystemBootstrapService::class.java)
    }
}
