package top.foxball.receptionbackendsystem.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseExportService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/excel")
class ExcelExportController(
    private val excelDatabaseExportService: ExcelDatabaseExportService,
) {
    @GetMapping("/export")
    fun exportByActivityId(
        @RequestParam activityId: Int,
        response: HttpServletResponse,
    ) {
        prepareDownloadResponse(response, "数字接待系统_数据导出.xlsx")
        excelDatabaseExportService.exportActivity(activityId, response.outputStream)
    }

    @GetMapping("/export-by-url")
    fun exportByActivityUrl(
        @RequestParam url: String,
        response: HttpServletResponse,
    ) {
        prepareDownloadResponse(response, "数字接待系统_数据导出.xlsx")
        excelDatabaseExportService.exportActivity(url, response.outputStream)
    }

    private fun prepareDownloadResponse(response: HttpServletResponse, filename: String) {
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.characterEncoding = "utf-8"
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''$encodedFilename")
    }
}
