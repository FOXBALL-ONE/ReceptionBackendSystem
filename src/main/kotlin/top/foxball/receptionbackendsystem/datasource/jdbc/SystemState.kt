package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 系统初始化状态（全局单例，[singletonId] 恒为 1）。
 *
 * 用 [initialized] 标记系统是否已完成初始化（Schema 就绪 + 至少一名管理员已建）。
 * `system_state` 无行视为未初始化；强制初始化（SYSTEM_FORCE_INIT）会刷新
 * [lastForceInitAt]/[lastForceInitBy] 审计字段。详见 SystemBootstrapService。
 */
@Table(name = "system_state")
@Entity
class SystemState(
    /** 单例主键，恒为 1（配合 DB CHECK 约束保证全局唯一行）。 */
    @Id
    @Column(name = "singleton_id", nullable = false)
    var singletonId: Short = 1,
) {
    /** 是否已初始化。 */
    @Column(name = "initialized", nullable = false)
    var initialized: Boolean = false

    /** 首次/最近一次完成初始化的时间。 */
    @Column(name = "initialized_at")
    var initializedAt: LocalDateTime? = null

    /** 触发初始化的操作者标识（默认取环境变量 SYSTEM_INIT_ACTOR）。 */
    @Column(name = "initialized_by", length = 255)
    var initializedBy: String? = null

    /** 应用/schema 版本，便于审计（取构建版本，不可用时为 null）。 */
    @Column(name = "schema_version", length = 64)
    var schemaVersion: String? = null

    /** 最近一次强制初始化的时间（仅 SYSTEM_FORCE_INIT 触发时写入）。 */
    @Column(name = "last_force_init_at")
    var lastForceInitAt: LocalDateTime? = null

    /** 最近一次强制初始化的操作者标识。 */
    @Column(name = "last_force_init_by", length = 255)
    var lastForceInitBy: String? = null

    companion object {
        /** 单例主键固定值。 */
        const val SINGLETON_ID: Short = 1
    }
}
