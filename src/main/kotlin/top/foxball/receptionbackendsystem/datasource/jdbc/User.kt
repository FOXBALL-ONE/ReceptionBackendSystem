package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.json.bind.annotation.JsonbTransient
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * 后台登录用户实体。
 *
 * 对应 users 表，保存后台账号的用户名、密码（BCrypt 哈希）与启用状态。
 */
@Table(name = "users")
@Entity
data class User(
    /** 用户主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 登录用户名，全局唯一。 */
    @Column(name = "username", nullable = false, unique = true, length = 64)
    var username: String,

    /** 登录密码（BCrypt 哈希），禁止明文落库或外泄。 */
    @JsonIgnore
    @JsonbTransient
    @Column(name = "password", nullable = false, length = 100)
    var password: String,

    /** 展示名，用于界面显示，未设置时回退到 username。 */
    @Column(name = "display_name", length = 64)
    var displayName: String? = null,

    /** 是否启用，禁用的账号无法登录。 */
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,
)
