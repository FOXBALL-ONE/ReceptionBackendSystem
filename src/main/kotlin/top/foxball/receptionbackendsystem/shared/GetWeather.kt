package top.foxball.receptionbackendsystem.shared

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

const val token = "HE2308192001221661"
class GetWeather {

    private fun getCity(
        city: String
    ): String{
        val client = OkHttpClient()
        val url = "https://nb3yfu3hjd.re.qweatherapi.com/geo/v2/city/lookup"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("location", city)
            .build()

        val responseBody = client.newCall(Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "MyApp/1.0")
            .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code}")
            }

            response.body?.string() ?: throw RuntimeException("响应体为空")
        }
        return responseBody
    }

    fun getWeather(
        city: String,
        day: Int
    ): String {
        val client = OkHttpClient()
        val city = getCity(city)
        val days = day.toString()+"d"
        val url = "https://nb3yfu3hjd.re.qweatherapi.com/v7/weather/$days"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("location", city)
            .build()

        val responseBody = client.newCall(Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code}")
            }

            response.body?.string() ?: throw RuntimeException("响应体为空")
        }

        return responseBody
    }

}
