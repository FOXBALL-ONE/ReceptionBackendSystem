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
 * 令牌带 ttype 声明区分用途：LOGIN 为正常登录令牌，TFA_CHALLENGE 为两步验证用的
 * 短时挑战令牌（仅可用于 /api/auth/login/totp|backup，不能访问受保护资源）。
 */
@Service
class JwtService(
    private val authProperties: AuthProperties,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret.toByteArray(Charsets.UTF_8))

    /** 令牌用途。 */
    enum class TokenType { LOGIN, TFA_CHALLENGE }

    /** 解析成功的令牌信息。 */
    data class ParsedJwt(
        val user: LoginUser,
        val jti: String,
        val exp: Long,
        val type: TokenType,
    )

    /**
     * 签发令牌。LOGIN 用 jwt-expiration-minutes，TFA_CHALLENGE 用 totp.challenge-minutes。
     */
    fun generate(user: LoginUser, type: TokenType = TokenType.LOGIN): String {
        val now = Date()
        val ttlMillis = if (type == TokenType.TFA_CHALLENGE) {
            authProperties.totp.challengeMinutes * MILLIS_PER_MINUTE
        } else {
            authProperties.jwtExpirationMinutes * MILLIS_PER_MINUTE
        }
        val expiration = Date(now.time + ttlMillis)
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.id?.toString())
            .claim(CLAIM_USERNAME, user.username)
            .claim(CLAIM_DISPLAY_NAME, user.displayName)
            .claim(CLAIM_TTYPE, type.name)
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
            val type = (claims[CLAIM_TTYPE] as? String)
                ?.let { runCatching { TokenType.valueOf(it) }.getOrNull() }
                ?: TokenType.LOGIN
            ParsedJwt(
                user = LoginUser(
                    id = claims.subject?.toLongOrNull(),
                    username = claims[CLAIM_USERNAME] as? String ?: "",
                    displayName = claims[CLAIM_DISPLAY_NAME] as? String,
                ),
                jti = claims.id ?: "",
                exp = claims.expiration?.time ?: 0L,
                type = type,
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
        private const val CLAIM_TTYPE = "ttype"
    }
}
