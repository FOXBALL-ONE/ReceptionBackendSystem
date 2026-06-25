package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 后台用户数据仓库。
 */
interface UserRepository : ReceptionRepository<User, Long> {
    /**
     * 根据用户名查询用户。
     */
    fun findByUsername(username: String): User?

    /**
     * 是否存在指定用户名的用户。
     */
    fun existsByUsername(username: String): Boolean
}
