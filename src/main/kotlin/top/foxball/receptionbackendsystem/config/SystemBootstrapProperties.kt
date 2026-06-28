package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 系统初始化（bootstrap）控制配置。
 *
 * 通过 application.yaml 的 `system.bootstrap.*` 前缀注入，支持环境变量覆盖。
 * 主开关 [forceInit] 对应 `.env` 的 `SYSTEM_FORCE_INIT`，用于强制下次启动重新执行初始化。
 */
@Component
@ConfigurationProperties(prefix = "system.bootstrap")
class SystemBootstrapProperties {

    /**
     * 强制初始化模式（来源 SYSTEM_FORCE_INIT）：
     * - 空 / `false` / `0`：正常，仅在未初始化时执行初始化；
     * - `true` / `1` / `reseed`：强制重跑幂等初始化步骤（admin 已存在则不动）；
     * - `admin-reset`：在上一项基础上按 auth.* 重建/重置管理员口令。
     */
    var forceInit: String = ""

    /** 审计默认操作者标识（来源 SYSTEM_INIT_ACTOR）。 */
    var auditActor: String = "env-operator"

    /**
     * 首次 / 强制初始化时是否走"清空重建（drop+create）"路径并要求控制台交互确认。
     * 来源 SYSTEM_BOOTSTRAP_REBUILD，默认开。关闭后退回安全增量 migrate（永不 drop），
     * 用于测试 / CI / 容器等无交互或不可破坏的场景。
     */
    var interactiveRebuild: Boolean = true

    /** 将 [forceInit] 文本解析为强类型模式。 */
    val forceMode: ForceInitMode
        get() = when (forceInit.trim().lowercase()) {
            "true", "1", "reseed" -> ForceInitMode.RESEED
            "admin-reset" -> ForceInitMode.ADMIN_RESET
            else -> ForceInitMode.NONE
        }
}

/** 强制初始化模式。 */
enum class ForceInitMode {
    /** 不强制：仅在未初始化时执行初始化。 */
    NONE,

    /** 强制重跑幂等初始化步骤（安全）。 */
    RESEED,

    /** 强制 + 重置管理员口令为 auth.password（破坏性，需重度审计）。 */
    ADMIN_RESET
}
