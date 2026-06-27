package top.foxball.receptionbackendsystem.controller

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ActivitiesService
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseExportService
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportOptions
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportResult
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Excel 模板下载 / 活动数据导出 / 批量导入入口。
 *
 * - GET  /template       下载空白导入模板（9 个 sheet 表头）
 * - GET  /export         按 id 或 url 导出某个活动的完整数据
 * - POST /import         上传 .xlsx 解析并落库（覆盖当前活动或创建新活动）
 */
@RestController
@RequestMapping("/api/excel")
class ExcelController(
    private val exportService: ExcelDatabaseExportService,
    private val importService: ExcelDatabaseImportService,
    private val activitiesService: ActivitiesService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {

    /** 下载空白导入模板（9 个 sheet 表头）。 */
    @GetMapping("/template")
    fun downloadTemplate(): ResponseEntity<ByteArrayResource> {
        val out = ByteArrayOutputStream()
        exportService.writeTemplate(out)
        return xlsxResponse(out.toByteArray(), "数字接待系统_导入模板.xlsx")
    }

    /** 按 id 或 url 导出某个活动的完整数据为 .xlsx。 */
    @GetMapping("/export")
    fun export(
        @RequestParam(name = "id", required = false) id: Long?,
        @RequestParam(name = "url", required = false) url: String?,
    ): ResponseEntity<ByteArrayResource> {
        if (id == null && url.isNullOrBlank()) {
            throw ParamErrorException("id 或 url 至少需要提供一个")
        }
        val out = ByteArrayOutputStream()
        when {
            id != null -> exportService.exportActivity(id, out)
            else -> exportService.exportActivity(url!!, out)
        }
        return xlsxResponse(out.toByteArray(), "数字接待系统_数据导出.xlsx")
    }

    /** 上传 .xlsx 解析并落库（覆盖当前活动或创建新活动）。 */
    @PostMapping("/import", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importExcel(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(name = "activityId", required = false) activityId: Long?,
        @RequestParam(name = "url", required = false) url: String?,
        @RequestParam(name = "name", required = false) name: String?,
        @RequestParam(name = "replaceExistingByUrl", required = false, defaultValue = "false") replaceExistingByUrl: Boolean,
    ): ResponseEntity<Response> {
        if (file.isEmpty) {
            return badRequest("请上传有效的 Excel 文件")
        }
        val options = buildOptions(activityId, url, name, replaceExistingByUrl)
        val result: ExcelDatabaseImportResult = importService.importExcel(file, options)
        return builder.ok().data(result).build()
    }

    /**
     * 传入 activityId 时：复用当前活动的 url 与基础信息，覆盖式重建（replaceExistingByUrl=true）。
     * 否则按 url/replaceExistingByUrl 处理；均缺省时由导入服务生成随机 url 创建新活动。
     */
    private fun buildOptions(
        activityId: Long?,
        url: String?,
        name: String?,
        replaceExistingByUrl: Boolean,
    ): ExcelDatabaseImportOptions {
        if (activityId != null) {
            val existing = activitiesService.findEntityById(activityId)
                ?: throw ResourceNotFoundException("活动不存在：$activityId")
            return ExcelDatabaseImportOptions(
                url = existing.url,
                name = name?.takeIf { it.isNotBlank() } ?: existing.name,
                masterTitle = existing.masterTitle,
                subtitle = existing.subtitle,
                startTime = existing.startTime,
                endTime = existing.endTime,
                bannerTags = existing.bannerTags,
                isAnimation = existing.isAnimation,
                isTopNavigationBar = existing.isTopNavigationBar,
                isOpen = existing.isOpen,
                replaceExistingByUrl = true,
            )
        }
        return ExcelDatabaseImportOptions(
            url = url?.takeIf { it.isNotBlank() },
            name = name?.takeIf { it.isNotBlank() },
            replaceExistingByUrl = replaceExistingByUrl,
        )
    }

    /** 构造 .xlsx 附件下载响应。 */
    private fun xlsxResponse(bytes: ByteArray, filename: String): ResponseEntity<ByteArrayResource> {
        val disposition = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(bytes.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(ByteArrayResource(bytes))
    }
}
