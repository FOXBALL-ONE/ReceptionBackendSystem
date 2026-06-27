package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 系统初始化状态仓库（单例行，主键恒为 [SystemState.SINGLETON_ID]）。
 */
interface SystemStateRepository : ReceptionRepository<SystemState, Short>
