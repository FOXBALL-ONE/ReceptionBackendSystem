package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.shared.GetWeather
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val getWeather: GetWeather,
    builder: ResponseBuilder,
) : ControllerSupport(builder) {

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
        const val WEATHER_FORECAST_DAYS = 15
    }
}

data class WeatherForecastRequest(
    val location: String? = null,
)
