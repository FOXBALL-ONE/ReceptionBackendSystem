package top.foxball.receptionbackendsystem.service

import jakarta.persistence.EntityManagerFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.info.BuildProperties
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
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
 * 以 [ApplicationRunner] 在应用启动序列中执行，负责 schema 与初始管理员就绪。
 * schema 由本服务编程式管理（`ddl-auto=none`），用 JPA/Hibernate 的
 * `SchemaManager`：[jakarta.persistence.SchemaManager.create] / [jakarta.persistence.SchemaManager.drop]
 * / [jakarta.persistence.SchemaManager.validate]（**无 migrate**）。
 *
 * 三条路径：
 * 1. **安全模式**（`interactive-rebuild=false`，用于测试 / CI / 容器）：缺失则建表，已有则校验；
 *    首次未初始化时播种管理员。**永不 drop、不警告、不倒计时**。
 * 2. **正常已初始化启动**：`validate`（校验表结构与实体一致；不一致则启动失败，提示需触发重建）。
 * 3. **需要重建 / 初始化**（首次未初始化，或 `SYSTEM_FORCE_INIT` 触发）：控制台醒目警告后
 *    **倒计时 [COUNTDOWN_SECONDS] 秒自动执行**（倒计时期间 Ctrl+C 中止），
 *    随后 `drop` 全部表 + `create` 重建 + 播种管理员 + 置位 `system_state`。
 *
 * 初始管理员账号 / 口令取自 auth.username / auth.password（默认 admin / admin123）。
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
    private val entityManagerFactory: EntityManagerFactory,
    private val jdbcTemplate: JdbcTemplate,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val force = bootstrapProperties.forceMode
        val tableExists = systemStateTableExists()
        val state = if (tableExists) stateRepository.findById(SystemState.SINGLETON_ID).orElse(null) else null
        val alreadyInitialized = state != null && state.initialized

        // 路径 1：安全模式 —— 不 drop、不警告、不倒计时
        if (!bootstrapProperties.interactiveRebuild) {
            if (!tableExists) {
                schemaCreate()
                ensureAdmin(ForceInitMode.NONE)
                recordState(ForceInitMode.NONE, null)
                logger.info("System bootstrap completed in safe mode (created schema; interactive-rebuild=false)")
            } else {
                schemaValidate()
                if (!alreadyInitialized) {
                    ensureAdmin(ForceInitMode.NONE)
                    recordState(ForceInitMode.NONE, state)
                } else {
                    logger.info("System already initialized at {}, schema validated", state!!.initializedAt)
                }
            }
            return
        }

        // 路径 2：正常已初始化且非强制 —— 校验 schema
        if (alreadyInitialized && force == ForceInitMode.NONE) {
            schemaValidate()
            logger.info("System already initialized at {}, schema validated", state!!.initializedAt)
            return
        }

        // 路径 3：需要重建 / 初始化 —— 警告 + 倒计时后执行
        val hasData = tableExists && anyTableHasRows()
        warnAndCountdown(hasData, force)
        schemaDropAndCreate()
        ensureAdmin(force)
        recordState(force, null)
        logCompletion(force)
    }

    /**
     * 确保存在按 auth.* 配置的管理员（幂等）。
     * 重建后 users 表为空 → 直接播种；ADMIN_RESET 分支仅在不重建（已存在用户）场景命中，保留以兼容。
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

    /**
     * 写入或更新 system_state 单例行。
     * - [existing] 为空（重建后表为空 / 首次无行）→ 新建；
     * - [existing] 非空 → 复用该行更新（避免主键冲突）。
     */
    private fun recordState(force: ForceInitMode, existing: SystemState?) {
        val now = LocalDateTime.now()
        val target = existing ?: SystemState()
        target.initialized = true
        target.initializedAt = now
        target.initializedBy = bootstrapProperties.auditActor
        target.schemaVersion = buildProperties.ifAvailable?.version
        if (force != ForceInitMode.NONE) {
            target.lastForceInitAt = now
            target.lastForceInitBy = bootstrapProperties.auditActor
        }
        stateRepository.saveAndFlush(target)
    }

    private fun logCompletion(force: ForceInitMode) {
        if (force != ForceInitMode.NONE) {
            logger.warn("SYSTEM_FORCE_INIT={} consumed; please remove it from .env after verification", force)
        } else {
            logger.info("System bootstrap completed (schema rebuilt + first initialization)")
        }
    }

    // ---------- schema 管理（JPA / Hibernate 编程式） ----------

    private fun schemaManager() =
        entityManagerFactory.unwrap(SessionFactoryImplementor::class.java).schemaManager

    /**
     * 校验表结构与实体一致；不一致抛出带中文提示的 IllegalStateException 使启动失败，
     * 指明原因与处理方式（设置 SYSTEM_FORCE_INIT 重建），避免维护者面对裸堆栈不知所措。
     */
    private fun schemaValidate() {
        try {
            schemaManager().validate()
        } catch (e: jakarta.persistence.SchemaValidationException) {
            throw IllegalStateException(
                "Schema 校验失败：数据库表结构与实体定义不一致。" +
                    "通常是因为变更了 @Entity 后未重建表。" +
                    "请设置 SYSTEM_FORCE_INIT=true（注意：会清空全部数据）后重启以重建 schema。" +
                    " 原始错误：${e.message}",
                e,
            )
        }
    }

    /** 建表（CREATE），调用方须确保表当前不存在（先 drop 或首次）。boolean=false 不动 database schema。 */
    private fun schemaCreate() {
        schemaManager().create(false)
    }

    /** 清空重建：DROP 全部表 → CREATE。 */
    private fun schemaDropAndCreate() {
        val sm = schemaManager()
        logger.warn("Dropping ALL schema objects (full rebuild) — all existing data will be lost")
        sm.drop(false)
        logger.warn("Creating schema from entity metadata")
        sm.create(false)
    }

    // ---------- 检测 ----------

    private fun systemStateTableExists(): Boolean =
        jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = ?)",
            Boolean::class.java,
            "system_state",
        ) ?: false

    /** 任意一张用户表存在非零行即视为"有数据"（重建将全部丢失）。 */
    private fun anyTableHasRows(): Boolean {
        val tables: List<String> = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = current_schema() AND table_type = 'BASE TABLE'",
            String::class.java,
        )
        return tables.any { table ->
            try {
                val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"$table\"", Long::class.java) ?: 0L
                count > 0L
            } catch (e: Exception) {
                // 视图 / 不可 count 的对象等异常 → 忽略，视为无数据
                false
            }
        }
    }

    // ---------- 警告 + 倒计时 ----------

    /** 输出醒目警告并倒计时 [COUNTDOWN_SECONDS] 秒后返回；倒计时期间可 Ctrl+C 中止。 */
    private fun warnAndCountdown(hasData: Boolean, force: ForceInitMode) {
        val action = when {
            force != ForceInitMode.NONE && hasData -> "清空重建（DROP+CREATE 全部表，所有数据永久丢失）"
            force != ForceInitMode.NONE -> "重建 schema（库当前为空，将建表并初始化）"
            hasData -> "重新初始化（检测到现有数据，drop+create 将全部清除）"
            else -> "首次初始化（建表并播种管理员）"
        }
        val dataLine = if (hasData) "存在数据（重建将全部丢失！）" else "空库（无可恢复数据）"
        val triggerLine = if (force != ForceInitMode.NONE) "SYSTEM_FORCE_INIT=$force" else "首次启动 / 未完成初始化"

        val out = System.out
        out.println()
        out.println(BANNER_RULE)
        out.println(BANNER_BLANK)
        out.println("!!   ⚠  系统 SCHEMA 重建 / 初始化 —— 即将自动执行")
        out.println(BANNER_BLANK)
        out.println("!!   即将执行：$action")
        out.println("!!   数据库当前：$dataLine")
        out.println("!!   触发原因：$triggerLine")
        out.println("!!   目标管理员账号：${authProperties.username}")
        out.println(BANNER_BLANK)
        out.println("!!   此操作不可逆。${COUNTDOWN_SECONDS} 秒后自动执行；如需中止请立即按 Ctrl+C。")
        out.println(BANNER_RULE)

        try {
            for (remaining in COUNTDOWN_SECONDS downTo 1) {
                out.print("\r!!   倒计时 ${remaining}s ...   ")
                out.flush()
                Thread.sleep(COUNTDOWN_TICK_MILLIS)
            }
        } catch (e: InterruptedException) {
            // 倒计时被中断（如关闭信号）→ 恢复中断标志并中止重建
            Thread.currentThread().interrupt()
            throw IllegalStateException("Schema rebuild countdown interrupted; aborting without changes", e)
        }
        out.println("\r!!   倒计时结束，开始执行重建。              ")
        out.flush()
        logger.warn("Countdown elapsed; proceeding with destructive schema rebuild")
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(SystemBootstrapService::class.java)
        private const val BANNER_WIDTH = 77
        private val BANNER_RULE: String = "!".repeat(BANNER_WIDTH)
        private val BANNER_BLANK: String = "!!" + " ".repeat(BANNER_WIDTH - 4) + "!!"

        /** 破坏性重建前的倒计时秒数。 */
        private const val COUNTDOWN_SECONDS = 10

        /** 倒计时每个 tick 的毫秒数。 */
        private const val COUNTDOWN_TICK_MILLIS = 1000L
    }
}
