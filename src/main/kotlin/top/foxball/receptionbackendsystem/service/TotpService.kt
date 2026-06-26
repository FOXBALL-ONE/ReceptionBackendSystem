package top.foxball.receptionbackendsystem.service

import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.config.AuthProperties
import java.net.URLEncoder

/**
 * TOTP（RFC 6238）能力：生成密钥、构造 otpauth URI、校验动态码。
 *
 * 底层使用 dev.samstevens.totp（仅依赖 commons-codec，不会引入 Jackson2，
 * 避免再次触发 Hibernate 的 JSON 列写出器从 Yasson 切到 Jackson2）。
 */
@Service
class TotpService(
    authProperties: AuthProperties,
) {
    private val secretGenerator = DefaultSecretGenerator()
    private val verifier = DefaultCodeVerifier(DefaultCodeGenerator(), SystemTimeProvider())
    private val issuer = authProperties.totp.issuer

    init {
        // 容忍 ±1 个 30s 周期的时钟漂移（仅有 setter，需显式调用）
        verifier.setAllowedTimePeriodDiscrepancy(1)
    }

    /** 生成新的 base32 共享密钥。 */
    fun generateSecret(): String = secretGenerator.generate()

    /**
     * 构造可被 authenticator 应用识别的 otpauth URI，前端用 qrcode 渲染二维码。
     */
    fun otpauthUri(secret: String, accountName: String): String {
        val enc = { value: String -> URLEncoder.encode(value, Charsets.UTF_8) }
        return buildString {
            append("otpauth://totp/")
            append(enc(issuer))
            append(":")
            append(enc(accountName))
            append("?secret=").append(secret)
            append("&issuer=").append(enc(issuer))
            append("&algorithm=SHA1")
            append("&digits=6")
            append("&period=30")
        }
    }

    /**
     * 校验动态码（容忍 ±1 周期时钟漂移）。非法输入返回 false 而非抛异常。
     */
    fun verify(secret: String, code: String): Boolean = try {
        verifier.isValidCode(secret, code)
    } catch (_: Exception) {
        false
    }
}
