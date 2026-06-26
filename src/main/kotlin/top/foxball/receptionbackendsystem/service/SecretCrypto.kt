package top.foxball.receptionbackendsystem.service

import org.springframework.stereotype.Component
import top.foxball.receptionbackendsystem.config.AuthProperties
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * TOTP 共享密钥的对称加解密（AES-GCM）。
 *
 * 密钥来自 auth.totp.secret-encryption-key，经 SHA-256 派生为 256 位 AES 密钥；
 * 这样即使数据库泄露，没有应用配置也无法还原动态码密钥。
 * 密文格式：Base64(iv(12) || ciphertext || tag(16))。
 */
@Component
class SecretCrypto(
    authProperties: AuthProperties,
) {
    private val keySpec: SecretKey =
        SecretKeySpec(sha256(authProperties.totp.secretEncryptionKey), "AES")

    private val random = SecureRandom()

    fun encrypt(plain: String): String {
        val iv = ByteArray(IV_LENGTH).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + cipherText)
    }

    fun decrypt(cipherBase64: String): String {
        val all = Base64.getDecoder().decode(cipherBase64)
        require(all.size > IV_LENGTH) { "invalid cipher" }
        val iv = all.copyOfRange(0, IV_LENGTH)
        val cipherText = all.copyOfRange(IV_LENGTH, all.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
    }

    private fun sha256(value: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))

    private companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH_BITS = 128
    }
}
