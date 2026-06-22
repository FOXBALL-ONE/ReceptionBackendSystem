package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.service.ActivitiesService
import top.foxball.receptionbackendsystem.service.QrcodeService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 活动配置接口。
 */
@RestController
@RequestMapping("/api/activities")
class ActivitiesController(
    private val activitiesService: ActivitiesService,
    private val qrcodeService: QrcodeService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部活动。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(activitiesService.findAll())

    /** 查询所有已开放活动。 */
    @PostMapping("/open")
    fun findOpenActivities(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(activitiesService.findOpenActivities())

    /** 根据主键查询活动。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(activitiesService.findById(request.id))

    /** 根据访问地址查询活动。 */
    @PostMapping("/find-by-url")
    fun findByUrl(@RequestBody request: UrlRequest): ResponseEntity<Response> =
        ok(activitiesService.findByUrl(request.url))

    /** 创建活动。 */
    @PostMapping("/create")
    fun create(@RequestBody activity: Activities): ResponseEntity<Response> =
        ok(activitiesService.create(activity), "活动创建成功")

    /** 更新活动。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateActivityRequest): ResponseEntity<Response> =
        ok(activitiesService.update(request.id, request.activity), "活动更新成功")

    /** 删除活动。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        activitiesService.delete(request.id)
        return ok(mapOf("id" to request.id), "活动删除成功")
    }

    /** 关闭活动。 */
    @PostMapping("/close")
    fun closeActivity(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(activitiesService.closeActivity(request.id), "活动已关闭")

    /** 开放活动。 */
    @PostMapping("/open-activity")
    fun openActivity(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(activitiesService.openActivity(request.id), "活动已开放")

    /** 搜索活动。 */
    @PostMapping("/search")
    fun searchActivities(@RequestBody request: SearchActivitiesRequest): ResponseEntity<Response> =
        ok(activitiesService.searchActivities(request.keyword, request.status))

    /** 获取活动统计信息。 */
    @PostMapping("/statistics")
    fun getStatistics(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(activitiesService.getStatistics())

    /** 生成活动二维码。 */
    @PostMapping("/qrcode")
    fun generateQrcode(@RequestBody request: QrcodeRequest): ResponseEntity<Response> {
        val activity = activitiesService.findById(request.id)
        val baseUrl = "https://example.com"
        val fullUrl = "$baseUrl${activity.url}"

        val qrcodeBase64 = qrcodeService.generateQrcodeBase64(fullUrl, request.size)

        val qrcodeData = mapOf(
            "qrcodeUrl" to "$baseUrl/qrcode/activity-${activity.id}.png",
            "qrcodeBase64" to qrcodeBase64,
            "url" to activity.url,
            "fullUrl" to fullUrl
        )

        return ok(qrcodeData)
    }

    /** 批量删除活动。 */
    @PostMapping("/batch-delete")
    fun batchDelete(@RequestBody request: BatchDeleteRequest): ResponseEntity<Response> =
        ok(activitiesService.batchDelete(request.ids), "批量删除完成")

    /** 复制活动。 */
    @PostMapping("/duplicate")
    fun duplicateActivity(@RequestBody request: DuplicateActivityRequest): ResponseEntity<Response> =
        ok(activitiesService.duplicateActivity(request.id, request.newUrl, request.newName), "复制成功")

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "success"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
