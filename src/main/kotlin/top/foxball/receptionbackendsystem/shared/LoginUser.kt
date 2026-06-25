package top.foxball.receptionbackendsystem.shared

/**
 * 登录态用户信息。
 *
 * 登录成功后写入 HttpSession，作为后续请求登录校验的凭证。
 * 由 users 表投影而来，不含密码等敏感字段。
 *
 * @param id 用户主键
 * @param username 登录用户名
 * @param displayName 展示名，未设置时回退到 username
 * @param loginAt 登录时间（毫秒时间戳）
 */
data class LoginUser(
    val id: Long? = null,
    val username: String,
    val displayName: String? = null,
    val loginAt: Long = System.currentTimeMillis(),
)
