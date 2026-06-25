package top.foxball.receptionbackendsystem.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.system.ApplicationHome
import org.springframework.stereotype.Component
import top.foxball.receptionbackendsystem.ReceptionBackendSystemApplication
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 图片存储配置。
 *
 * storage：图片物理存储根目录，由环境变量 IMAGE_STORAGE 提供（通常写在 .env 文件中，
 * 亦可作为系统环境变量注入）。
 *  - 设置了 IMAGE_STORAGE 时以其值为准；
 *  - 未设置时回退到应用程序所在目录下的 img 子目录，
 *    例如 /opt/java/app.jar → /opt/java/img，便于生产部署按部署目录归集上传文件。
 *
 * baseUrl：图片对外访问的 URL 前缀，与物理存储目录解耦。
 */
@Component
data class ImageStorageProperties(
    @Value("\${IMAGE_STORAGE:#{null}}")
    val storage: String?,

    @Value("\${image.base-url:\${image.baseUrl:\${Image.BaseURL:http://localhost:8080/images}}}")
    val baseUrl: String,
) {
    fun storageRoot(): Path {
        val configured = storage?.takeIf { it.isNotBlank() }
        val root = if (configured != null) {
            Paths.get(configured)
        } else {
            // 应用打包为 jar 运行时为 jar 所在目录，IDE 中为 classes 目录
            ApplicationHome(ReceptionBackendSystemApplication::class.java).dir.toPath().resolve(DEFAULT_SUBDIR)
        }
        return root.toAbsolutePath().normalize()
    }

    private companion object {
        const val DEFAULT_SUBDIR = "img"
    }
}
