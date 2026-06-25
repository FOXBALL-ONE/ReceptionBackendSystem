package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 后台登录模块配置。
 *
 * 通过 application.yaml 的 `auth.*` 前缀注入，并支持环境变量覆盖。
 * username / password 仅作为首次启动写入 users 表的默认管理员种子值
 * （数据库表因 ddl-auto=create 每次启动重建，详见 UserDataInitializer）；
 * 真正的登录校验始终以 users 表为准。
 *
 * jwtSecret / jwtExpirationMinutes 控制 JWT 的签发与校验；
 * jwtSecret 需 ≥ 32 字节（HS256 要求），生产环境务必通过环境变量覆盖。
 */
@Component
@ConfigurationProperties(prefix = "auth")
class AuthProperties {

    /** 默认管理员用户名（种子），生产环境建议通过 AUTH_USERNAME 环境变量覆盖 */
    var username: String = "admin"

    /** 默认管理员密码明文（种子，入库时会被 BCrypt 哈希），生产环境建议通过 AUTH_PASSWORD 环境变量覆盖 */
    var password: String = "admin123"

    /** JWT 签名密钥（HS256，需 ≥ 32 字节）；变更后已签发的令牌全部失效 */
    var jwtSecret: String = "reception-backend-dev-jwt-secret-please-override-in-production"

    /** JWT 有效期（分钟），默认 7 天 */
    var jwtExpirationMinutes: Long = 7 * 24 * 60L
}
