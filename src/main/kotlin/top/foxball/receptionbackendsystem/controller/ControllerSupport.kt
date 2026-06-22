package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import top.foxball.receptionbackendsystem.service.ReceptionService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

abstract class ControllerSupport(
    private val responseBuilder: ResponseBuilder,
) {
    protected fun ok(data: Any?): ResponseEntity<Response> =
        responseBuilder.ok().data(data).build()

    protected fun badRequest(message: String): ResponseEntity<Response> =
        responseBuilder.badRequest().message(message).build()

    protected fun notFound(message: String): ResponseEntity<Response> =
        responseBuilder.notFound().message(message).build()

    protected fun <T : Any, ID : Any> findById(
        id: ID?,
        service: ReceptionService<T, ID>,
    ): ResponseEntity<Response> {
        if (id == null) {
            return badRequest("id is required")
        }
        return ok(service.findEntityById(id))
    }

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

    protected fun requireActivityId(activityId: Int?): Int? = activityId
}
