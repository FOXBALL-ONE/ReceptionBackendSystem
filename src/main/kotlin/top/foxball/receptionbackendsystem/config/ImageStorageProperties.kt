package top.foxball.receptionbackendsystem.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
data class ImageStorageProperties(
    @Value("\${image.storage:\${Image.storage:./images}}")
    val storage: String,

    @Value("\${image.base-url:\${image.baseUrl:\${Image.BaseURL:http://localhost:8080/images}}}")
    val baseUrl: String,
) {
    fun storageRoot(): Path =
        Paths.get(storage).toAbsolutePath().normalize()
}
