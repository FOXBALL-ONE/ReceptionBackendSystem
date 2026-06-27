package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import top.foxball.receptionbackendsystem.service.ReceptionService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 业务控制器的抽象基类。
 *
 * 封装统一的成功/错误响应与按 id 查询/删除的通用逻辑，子类通过继承复用 ResponseBuilder 与通用方法。
 */
abstract class ControllerSupport(
    private val responseBuilder: ResponseBuilder,
) {
    /** 包装成功响应：返回 200 并携带 data。 */
    protected fun ok(data: Any?): ResponseEntity<Response> =
        responseBuilder.ok().data(data).build()

    /** 包装参数错误响应：返回 400 并携带错误消息。 */
    protected fun badRequest(message: String): ResponseEntity<Response> =
        responseBuilder.badRequest().message(message).build()

    /** 包装资源不存在响应：返回 404 并携带错误消息。 */
    protected fun notFound(message: String): ResponseEntity<Response> =
        responseBuilder.notFound().message(message).build()

    /** 按 id 查询单条记录，id 缺失返回 badRequest，命中则返回实体（可能为 null）。 */
    protected fun <T : Any, ID : Any> findById(
        id: ID?,
        service: ReceptionService<T, ID>,
    ): ResponseEntity<Response> {
        if (id == null) {
            return badRequest("id is required")
        }
        return ok(service.findEntityById(id))
    }

    /** 按 id 删除单条记录，id 缺失返回 badRequest，实体不存在返回 notFound。 */
    protected fun <T : Any, ID : Any> deleteById(
        id: ID?,
        service: ReceptionService<T, ID>,
    ): ResponseEntity<Response> {
        if (id == null) {
            return badRequest("id is required")
        }
        val entity = service.findEntityById(id)
            ?: return notFound("entity not found")
        service.deleteOne(entity)
        return ok(true)
    }

    /** 按 id 列表批量删除，返回请求与实际删除条数的统计。 */
    protected fun <T : Any, ID : Any> deleteByIds(
        ids: List<ID>,
        service: ReceptionService<T, ID>,
    ): ResponseEntity<Response> {
        if (ids.isEmpty()) {
            return badRequest("ids is required")
        }
        val entities = ids.mapNotNull(service::findEntityById)
        service.deleteBatch(entities)
        return ok(
            mapOf(
                "requested" to ids.size,
                "deleted" to entities.size,
            )
        )
    }

    /** 透传 activityId，预留为子类参数校验的扩展点。 */
    protected fun requireActivityId(activityId: Long?): Long? = activityId
}
