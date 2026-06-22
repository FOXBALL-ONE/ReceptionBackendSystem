package top.foxball.receptionbackendsystem.service.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.service.QrcodeService
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

/**
 * 二维码生成服务实现。
 */
@Service
class QrcodeServiceImpl : QrcodeService {

    override fun generateQrcodeBase64(content: String, size: Int): String {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until size) {
            for (y in 0 until size) {
                image.setRGB(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        val imageBytes = outputStream.toByteArray()
        val base64String = Base64.getEncoder().encodeToString(imageBytes)

        return "data:image/png;base64,$base64String"
    }
}
