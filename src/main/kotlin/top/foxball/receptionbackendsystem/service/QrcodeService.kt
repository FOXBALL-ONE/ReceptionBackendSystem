package top.foxball.receptionbackendsystem.service

/**
 * 二维码生成服务。
 */
interface QrcodeService {
    /**
     * 生成二维码图片的 Base64 编码。
     *
     * @param content 二维码内容
     * @param size 二维码尺寸（像素）
     * @return Base64 编码的二维码图片
     */
    fun generateQrcodeBase64(content: String, size: Int = 256): String
}
