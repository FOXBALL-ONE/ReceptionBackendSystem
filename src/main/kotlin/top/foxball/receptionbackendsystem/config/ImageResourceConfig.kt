package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Files

/**
 * 图片静态资源访问配置。
 *
 * 将 [ImageProperties.baseUrl] 中的 URL path 映射到 [ImageProperties.storageRoot]，
 * 让 API 返回的图片地址可以被浏览器直接读取或下载。
 */
@Configuration
@EnableConfigurationProperties(ImageProperties::class)
class ImageResourceConfig(
    private val imageProperties: ImageProperties,
) : WebMvcConfigurer {

    /**
     * 注册图片静态资源处理器。
     *
     * 应用启动时会先创建本地存储目录，随后把公开访问路径映射到该目录。
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        Files.createDirectories(imageProperties.storageRoot)
        registry.addResourceHandler(imageProperties.resourceHandlerPattern)
            .addResourceLocations(imageProperties.resourceLocation)
    }
}
