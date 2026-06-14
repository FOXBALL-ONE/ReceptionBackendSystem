package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ReceptionViewService

/**
 * 数字接待系统前端视图接口。
 * 为 index-red.html 前端提供统一的数据接口。
 */
@RestController
@RequestMapping("/api")
class ReceptionViewController(
    private val receptionViewService: ReceptionViewService
) {

    @GetMapping("/site/{activityUrl}/{section}")
    fun getByActivityUrl(
        @PathVariable activityUrl: String,
        @PathVariable section: String,
    ): ResponseEntity<Any> {
        val data = when (section) {
            "meta" -> receptionViewService.getMeta(activityUrl)
            "schedule" -> receptionViewService.getSchedule(activityUrl)
            "people" -> receptionViewService.getPeople(activityUrl)
            "vehicles" -> receptionViewService.getVehicles(activityUrl)
            "meals" -> receptionViewService.getMeals(activityUrl)
            "hotel" -> receptionViewService.getHotel(activityUrl)
            "sites" -> receptionViewService.getSites(activityUrl)
            "service" -> receptionViewService.getService(activityUrl)
            "overview" -> receptionViewService.getOverview(activityUrl)
            else -> throw ResourceNotFoundException("展示数据接口不存在：$section")
        }
        return ResponseEntity.ok(data)
    }

    /**
     * 获取元数据配置（活动标题、logo、横幅图等）。
     * GET /api/meta
     */
    @GetMapping("/meta")
    fun getMeta(): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(receptionViewService.getMeta())
    }

    /**
     * 获取日程安排数据。
     * GET /api/schedule
     */
    @GetMapping("/schedule")
    fun getSchedule(): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(receptionViewService.getSchedule())
    }

    /**
     * 获取人员信息列表。
     * GET /api/people
     */
    @GetMapping("/people")
    fun getPeople(): ResponseEntity<List<Map<String, Any?>>> {
        return ResponseEntity.ok(receptionViewService.getPeople())
    }

    /**
     * 获取车辆安排信息。
     * GET /api/vehicles
     */
    @GetMapping("/vehicles")
    fun getVehicles(): ResponseEntity<List<Map<String, Any?>>> {
        return ResponseEntity.ok(receptionViewService.getVehicles())
    }

    /**
     * 获取用餐安排信息。
     * GET /api/meals
     */
    @GetMapping("/meals")
    fun getMeals(): ResponseEntity<List<Map<String, Any?>>> {
        return ResponseEntity.ok(receptionViewService.getMeals())
    }

    /**
     * 获取住宿信息。
     * GET /api/hotel
     */
    @GetMapping("/hotel")
    fun getHotel(): ResponseEntity<List<Map<String, Any?>>> {
        return ResponseEntity.ok(receptionViewService.getHotel())
    }

    /**
     * 获取考察点信息。
     * GET /api/sites
     */
    @GetMapping("/sites")
    fun getSites(): ResponseEntity<List<Map<String, Any?>>> {
        return ResponseEntity.ok(receptionViewService.getSites())
    }

    /**
     * 获取服务信息（工作人员、天气、须知等）。
     * GET /api/service
     */
    @GetMapping("/service")
    fun getService(): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(receptionViewService.getService())
    }

    /**
     * 获取概况介绍。
     * GET /api/overview
     */
    @GetMapping("/overview")
    fun getOverview(): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(receptionViewService.getOverview())
    }
}
