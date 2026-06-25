package top.foxball.receptionbackendsystem.shared

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.jacksonObjectMapper

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
            throw RuntimeException("未找到城市: $city")
        }

        return locations.get(0)
            .path("id")
            .asString()
            .takeIf { it.isNotBlank() }
            ?: throw RuntimeException("城市 ID 为空: $city")
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

    private fun apiUrl(path: String) =
        "${baseUrl.trimEnd('/')}/${path.trimStart('/')}".toHttpUrl()

    private companion object {
        const val QWEATHER_API_KEY_HEADER = "X-QW-Api-Key"
    }

}
