package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.shared.GetWeather
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 天气预报接口。
 *
 * 通过 [GetWeather] 拉取指定地点的未来天气数据并回传给前端展示页。
 */
@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val getWeather: GetWeather,
    builder: ResponseBuilder,
) : ControllerSupport(builder) {

    /** 按地点查询未来天气预报。 */
    @PostMapping("/forecast")
    fun forecast(@RequestBody request: WeatherForecastRequest): ResponseEntity<Response> {
        val location = request.location?.trim()
            ?: return badRequest("location is required")
        if (location.isBlank()) {
            return badRequest("location is required")
        }

        return ok(getWeather.getWeatherForecast(location, WEATHER_FORECAST_DAYS))
    }

    private companion object {
        /** 预报天数。 */
        const val WEATHER_FORECAST_DAYS = 15
    }
}

/** 天气预报请求体 */
data class WeatherForecastRequest(
    val location: String? = null,
)
