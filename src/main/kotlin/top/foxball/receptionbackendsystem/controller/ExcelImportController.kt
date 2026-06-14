package top.foxball.receptionbackendsystem.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportOptions
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.time.LocalDateTime

/**
 * Excel 数据导入接口。
 */
@RestController
@RequestMapping("/api/excel")
class ExcelImportController(
    private val excelDatabaseImportService: ExcelDatabaseImportService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 上传 Excel 并导入为一个活动。 */
    @PostMapping("/import")
    fun importExcel(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) url: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) masterTitle: String?,
        @RequestParam(required = false) subtitle: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startTime: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endTime: LocalDateTime?,
        @RequestParam(required = false) bannerTags: String?,
        @RequestParam(required = false) isAnimation: Boolean?,
        @RequestParam(required = false) isTopNavigationBar: Boolean?,
        @RequestParam(required = false) isOpen: Boolean?,
        @RequestParam(defaultValue = "false") replaceExistingByUrl: Boolean,
    ): ResponseEntity<Response> {
        if (file.isEmpty) {
            throw ParamErrorException("导入文件不能为空")
        }

        val result = excelDatabaseImportService.importExcel(
            file,
            ExcelDatabaseImportOptions(
                url = url.trimToNull(),
                name = name.trimToNull(),
                masterTitle = masterTitle.trimToNull(),
                subtitle = subtitle.trimToNull(),
                startTime = startTime,
                endTime = endTime,
                bannerTags = bannerTags.trimToNull(),
                isAnimation = isAnimation ?: true,
                isTopNavigationBar = isTopNavigationBar ?: true,
                isOpen = isOpen ?: true,
                replaceExistingByUrl = replaceExistingByUrl,
            )
        )

        return responseBuilder.ok()
            .message("Excel导入成功")
            .data(result)
            .build()
    }

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
