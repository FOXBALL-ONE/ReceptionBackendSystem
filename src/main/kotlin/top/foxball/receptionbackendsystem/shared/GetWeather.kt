package top.foxball.receptionbackendsystem.shared

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class GetWeather(
    @Value("\${qweather.base-url}")
    private val baseUrl: String,

    @Value("\${qweather.api-key}")
    private val apiKey: String,
) {
    private val client = OkHttpClient()
    private val objectMapper = jacksonObjectMapper()

    private fun getCityId(
        city: String
    ): String {
        val url = apiUrl("geo/v2/city/lookup")
            .newBuilder()
            .addQueryParameter("location", city)
            .build()

        val responseBody = client.newCall(Request.Builder()
            .url(url)
            .get()
            .addHeader(QWEATHER_API_KEY_HEADER, apiKey)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "MyApp/1.0")
            .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code}")
            }

            response.body.string()
        }

        val locations = objectMapper.readTree(responseBody).path("location")
        if (!locations.isArray || locations.isEmpty) {
            throw IllegalArgumentException("未找到城市: $city")
        }

        return locations.get(0)
            .path("id")
            .asString()
            .takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("城市 ID 为空: $city")
    }

    fun getWeather(
        city: String,
        day: Int
    ): String {
        val cityId = getCityId(city)
        val days = "${day}d"
        val url = apiUrl("v7/weather/$days")
            .newBuilder()
            .addQueryParameter("location", cityId)
            .build()

        val responseBody = client.newCall(Request.Builder()
            .url(url)
            .get()
            .addHeader(QWEATHER_API_KEY_HEADER, apiKey)
            .addHeader("Accept", "application/json")
            .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code}")
            }

            response.body.string()
        }

        return responseBody
    }

    fun getWeatherForecast(
        city: String,
        day: Int = 15,
    ): List<WeatherForecastItem> {
        val cityName = city.trim()
        require(cityName.isNotBlank()) { "地点不能为空" }

        val responseBody = getWeather(cityName, day)
        val root = objectMapper.readTree(responseBody)
        val code = root.path("code").asString()
        if (code != QWEATHER_SUCCESS_CODE) {
            throw RuntimeException("天气服务返回异常: $code")
        }

        val daily = root.path("daily")
        if (!daily.isArray || daily.isEmpty) {
            return emptyList()
        }

        return (0 until daily.size()).map { index ->
            val item = daily.get(index)
            val fxDate = item.path("fxDate").asString().takeIf { it.isNotBlank() }
            val tempMin = item.path("tempMin").asString().takeIf { it.isNotBlank() }
            val tempMax = item.path("tempMax").asString().takeIf { it.isNotBlank() }
            val textDay = item.path("textDay").asString().takeIf { it.isNotBlank() }
            val textNight = item.path("textNight").asString().takeIf { it.isNotBlank() }

            WeatherForecastItem(
                time = fxDate?.let { LocalDate.parse(it).atStartOfDay() },
                city = cityName,
                temperature = listOfNotNull(tempMin, tempMax)
                    .joinToString("℃ - ", postfix = if (tempMin != null || tempMax != null) "℃" else ""),
                weatherDescriptor = when {
                    textDay.isNullOrBlank() -> textNight.orEmpty()
                    textNight.isNullOrBlank() || textDay == textNight -> textDay
                    else -> "${textDay}转${textNight}"
                },
            )
        }
    }

    private fun apiUrl(path: String) =
        "${baseUrl.trimEnd('/')}/${path.trimStart('/')}".toHttpUrl()

    private companion object {
        const val QWEATHER_API_KEY_HEADER = "X-QW-Api-Key"
        const val QWEATHER_SUCCESS_CODE = "200"
    }

}

data class WeatherForecastItem(
    val time: LocalDateTime?,
    val city: String,
    val temperature: String,
    val weatherDescriptor: String,
)
