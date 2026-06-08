package top.foxball.receptionbackendsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 图片存储配置。
 *
 * 负责把配置文件中的 `Image.storage` 和 `Image.BaseURL` 绑定为后端可使用的路径配置，
 * 并集中处理“相对路径入库、绝对磁盘路径落盘、公开 URL 返回”的转换逻辑。
 */
@ConfigurationProperties(prefix = "image")
data class ImageProperties(
    /** 图片文件在服务器本地磁盘上的基础存储目录。 */
    var storage: String = "./images",

    /** 图片对外访问的 URL 前缀，API 返回图片地址时会用它拼接相对路径。 */
    var baseUrl: String = "http://localhost:8080/images/",
) {
    /** 规范化后的本地存储根目录，所有图片文件都必须位于该目录下。 */
    val storageRoot: Path
        get() = Paths.get(storage.ifBlank { "./images" }).toAbsolutePath().normalize()

    /** Spring 静态资源映射使用的文件系统资源地址。 */
    val resourceLocation: String
        get() = storageRoot.toUri().toString()

    /** Spring MVC 静态资源访问匹配规则，例如匹配 `/images/` 下的所有路径。 */
    val resourceHandlerPattern: String
        get() = "${publicPathPrefix().removeSuffix("/")}/**"

    /**
     * 根据数据库中的相对路径解析真实存储路径。
     *
     * 返回路径一定在 [storageRoot] 下，避免 `../` 或绝对路径造成越权文件访问。
     */
    fun resolveStoragePath(relativePath: String): Path {
        val normalizedRelativePath = normalizeRelativePath(relativePath)
        val resolvedPath = storageRoot.resolve(normalizedRelativePath).normalize()
        require(resolvedPath.startsWith(storageRoot)) { "图片路径不能超出存储目录" }
        return resolvedPath
    }

    /**
     * 将图片相对路径转换成前端可直接访问的公开 URL。
     */
    fun accessUrl(relativePath: String): String {
        val normalizedRelativePath = normalizeRelativePath(relativePath)
        val normalizedBaseUrl = baseUrl.ifBlank { "http://localhost:8080/images/" }.trimEnd('/')
        return "$normalizedBaseUrl/$normalizedRelativePath"
    }

    /**
     * 将任意图片字段转换为前端可用 URL。
     *
     * 已经是完整外链的值会原样返回；相对路径会转换为 [accessUrl]。
     */
    fun publicUrl(rawPath: String?): String? {
        val value = rawPath?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (isExternalUrl(value)) {
            return value
        }
        return accessUrl(value)
    }

    /**
     * 将传入路径统一转换为数据库可保存的相对路径。
     *
     * 支持去除公开访问前缀、去除存储根目录，并统一使用 `/` 作为路径分隔符。
     */
    fun normalizeRelativePath(rawPath: String): String {
        val pathWithoutPublicBase = removePublicBase(rawPath.trim())
        val pathFromStorageRoot = removeStorageRoot(pathWithoutPublicBase)
        val slashNormalized = pathFromStorageRoot.replace('\\', '/').trimStart('/')

        require(slashNormalized.isNotBlank()) { "图片相对路径不能为空" }
        require(!slashNormalized.matches(Regex("^[A-Za-z]:.*"))) { "图片路径必须是相对路径" }

        val normalizedPath = Paths.get(slashNormalized).normalize()
        require(!normalizedPath.isAbsolute && !normalizedPath.startsWith("..")) { "图片路径必须是相对路径" }

        return normalizedPath.toString().replace('\\', '/')
    }

    /** 如果传入的是公开访问 URL 或 URL path，则去掉公开访问前缀，只保留图片相对路径。 */
    private fun removePublicBase(rawPath: String): String {
        val configuredBaseUrl = baseUrl.ifBlank { "http://localhost:8080/images/" }
        if (rawPath.startsWith(configuredBaseUrl)) {
            return rawPath.removePrefix(configuredBaseUrl).trimStart('/', '\\')
        }

        val publicPrefix = publicPathPrefix()
        val requestPath = runCatching { URI(rawPath).path }.getOrNull()
            ?: rawPath

        return if (requestPath.startsWith(publicPrefix)) {
            requestPath.removePrefix(publicPrefix).trimStart('/', '\\')
        } else {
            rawPath
        }
    }

    /** 如果传入的是存储根目录下的绝对磁盘路径，则转换成相对路径。 */
    private fun removeStorageRoot(rawPath: String): String {
        val path = runCatching { Paths.get(rawPath) }.getOrNull()
            ?: return rawPath
        if (!path.isAbsolute) {
            return rawPath
        }

        val absolutePath = path.normalize()

        return if (absolutePath.startsWith(storageRoot)) {
            storageRoot.relativize(absolutePath).toString()
        } else {
            rawPath
        }
    }

    /** 从 `baseUrl` 中提取 Spring 静态资源映射所需的 URL path 前缀。 */
    private fun publicPathPrefix(): String {
        val configuredBaseUrl = baseUrl.ifBlank { "http://localhost:8080/images/" }
        val path = runCatching { URI(configuredBaseUrl).path }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: configuredBaseUrl

        val normalizedPath = path.trim().trim('/')
        return "/${normalizedPath.ifBlank { "images" }}/"
    }

    /** 判断当前值是否已经是前端可直接使用的完整外部 URL。 */
    private fun isExternalUrl(value: String): Boolean {
        val scheme = runCatching { URI(value).scheme?.lowercase() }.getOrNull()
        return scheme == "http" || scheme == "https" || scheme == "data" || scheme == "blob"
    }
}
