package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 颜色标签数据仓库。
 */
interface ColorTagRepository : JpaRepository<ColorTag, Int> {
    /**
     * 根据标签名称查询颜色标签。
     */
    fun findByName(name: String): ColorTag?

    /**
     * 判断指定标签名称是否已存在。
     */
    fun existsByName(name: String): Boolean
}
