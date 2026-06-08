package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 提示服务配置数据仓库。
 */
interface PromptServiceRepository : JpaRepository<PromptService, Int>
