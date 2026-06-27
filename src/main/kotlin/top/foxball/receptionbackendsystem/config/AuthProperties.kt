package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 后台登录模块配置。
 *
 * 通过 application.yaml 的 `auth.*` 前缀注入，并支持环境变量覆盖。
 * username / password 作为初始化时播种到 users 表的默认管理员种子值
 * （AUTH_USERNAME / AUTH_PASSWORD 未设置时默认 admin / admin123；初始化详见 SystemBootstrapService）；
 * 真正的登录校验始终以 users 表为准。
 *
 * jwtSecret / jwtExpirationMinutes 控制 JWT 的签发与校验；
 * jwtSecret 仅由环境变量 AUTH_JWT_SECRET 注入（≥ 32 字节，HS256 要求），无默认值、未设置则启动失败。
 */
@Component
@ConfigurationProperties(prefix = "auth")
class AuthProperties {

    /** 默认管理员用户名（种子），生产环境建议通过 AUTH_USERNAME 环境变量覆盖 */
    var username: String = "admin"

    /** 默认管理员密码明文（种子，入库时会被 BCrypt 哈希），生产环境建议通过 AUTH_PASSWORD 环境变量覆盖 */
    var password: String = "admin123"

    /**
     * JWT 签名密钥（HS256，需 ≥ 32 字节）；仅由环境变量 AUTH_JWT_SECRET 注入，无默认值。
     * 变更后已签发的令牌全部失效。
     */
    lateinit var jwtSecret: String

    /** JWT 有效期（分钟），默认 7 天 */
    var jwtExpirationMinutes: Long = 7 * 24 * 60L

    /** TOTP 两步验证配置。 */
    var totp: Totp = Totp()

    /** TOTP 两步验证配置项。 */
    class Totp {
        /** authenticator 中显示的发行方名称。 */
        var issuer: String = "接待管理系统"

        /** 两步验证挑战令牌有效期（分钟）。 */
        var challengeMinutes: Long = 5

        /** TOTP 密钥加密密钥（经 SHA-256 派生为 AES-256）；仅由环境变量 AUTH_TOTP_ENC_KEY 注入，无默认值。 */
        lateinit var secretEncryptionKey: String
    }
}
