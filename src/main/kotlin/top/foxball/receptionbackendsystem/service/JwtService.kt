package top.foxball.receptionbackendsystem.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.config.AuthProperties
import top.foxball.receptionbackendsystem.shared.LoginUser
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

/**
 * JWT 签发与解析服务。
 *
 * 使用 HMAC-SHA256 对称签名，密钥来自 auth.jwtSecret（内存中持有，无需 Redis）。
 * 令牌自包含用户信息与有效期，签名校验通过即视为合法，服务端重启后旧令牌仍可用。
 */
@Service
class JwtService(
    private val authProperties: AuthProperties,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret.toByteArray(Charsets.UTF_8))

    /** 解析成功的令牌信息。 */
    data class ParsedJwt(
        val user: LoginUser,
        val jti: String,
        val exp: Long,
    )

    /**
     * 为登录用户签发一个新令牌。
     */
    fun generate(user: LoginUser): String {
        val now = Date()
        val expiration = Date(now.time + authProperties.jwtExpirationMinutes * MILLIS_PER_MINUTE)
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.id?.toString())
            .claim(CLAIM_USERNAME, user.username)
            .claim(CLAIM_DISPLAY_NAME, user.displayName)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    /**
     * 校验签名与有效期并解析声明；令牌非法或过期返回 null。
     */
    fun parse(token: String): ParsedJwt? {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            ParsedJwt(
                user = LoginUser(
                    id = claims.subject?.toLongOrNull(),
                    username = claims[CLAIM_USERNAME] as? String ?: "",
                    displayName = claims[CLAIM_DISPLAY_NAME] as? String,
                ),
                jti = claims.id ?: "",
                exp = claims.expiration?.time ?: 0L,
            )
        } catch (_: JwtException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val CLAIM_USERNAME = "username"
        private const val CLAIM_DISPLAY_NAME = "displayName"
    }
}
