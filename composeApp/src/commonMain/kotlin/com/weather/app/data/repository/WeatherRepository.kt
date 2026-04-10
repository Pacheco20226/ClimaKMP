package com.weather.app.data.repository

import com.weather.app.data.api.WeatherApiClient
import com.weather.app.data.model.CurrentWeatherDto
import com.weather.app.data.model.ForecastDto
import com.weather.app.domain.model.ForecastDay
import com.weather.app.domain.model.WeatherInfo
import kotlinx.datetime.*

class WeatherRepository(private val api: WeatherApiClient) {

    private val useMock = WeatherApiClient.API_KEY == "YOUR_API_KEY_HERE"

    suspend fun getByCity(city: String): Result<WeatherInfo> =
        if (useMock) Result.success(mockWeather(city))
        else runCatching {
            map(api.currentByCity(city), api.forecastByCity(city))
        }

    suspend fun getByCoords(lat: Double, lon: Double): Result<WeatherInfo> =
        if (useMock) Result.success(mockWeather("Mi ubicación"))
        else runCatching {
            map(api.currentByCoords(lat, lon), api.forecastByCoords(lat, lon))
        }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private fun map(c: CurrentWeatherDto, f: ForecastDto): WeatherInfo {
        val now = Clock.System.now().epochSeconds
        return WeatherInfo(
            cityName    = c.name,
            country     = c.sys.country,
            temperature = c.main.temp,
            feelsLike   = c.main.feelsLike,
            humidity    = c.main.humidity,
            pressure    = c.main.pressure,
            windSpeed   = c.wind.speed,
            windDegree  = c.wind.deg,       // Act 3.1 — Grados del viento desde la API
            visibility  = c.visibility / 1000,
            uvIndex     = 0.0,
            description = c.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
            iconCode    = c.weather.firstOrNull()?.icon ?: "01d",
            sunrise     = c.sys.sunrise,
            sunset      = c.sys.sunset,
            isDay       = now in c.sys.sunrise..c.sys.sunset,
            lat         = c.coord.lat,
            lon         = c.coord.lon,
            forecast    = buildForecast(f)
        )
    }

    private fun buildForecast(f: ForecastDto): List<ForecastDay> =
        f.list.groupBy { it.dtTxt.take(10) }.entries.take(5).map { (_, items) ->
            val mid = items.minByOrNull {
                kotlin.math.abs(it.dtTxt.substring(11, 13).toInt() - 12)
            } ?: items.first()
            ForecastDay(
                date        = mid.dt,
                dayLabel    = dayLabel(mid.dt),
                tempMin     = items.minOf { it.main.temp },
                tempMax     = items.maxOf { it.main.temp },
                description = mid.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                iconCode    = mid.weather.firstOrNull()?.icon ?: "01d",
                humidity    = mid.main.humidity,
                windSpeed   = mid.wind.speed,
                rainChance  = (items.maxOf { it.pop } * 100).toInt()
            )
        }

    private fun dayLabel(epoch: Long): String {
        val dow = Instant.fromEpochSeconds(epoch)
            .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek
        return when (dow) {
            DayOfWeek.MONDAY    -> "Lun"
            DayOfWeek.TUESDAY   -> "Mar"
            DayOfWeek.WEDNESDAY -> "Mié"
            DayOfWeek.THURSDAY  -> "Jue"
            DayOfWeek.FRIDAY    -> "Vie"
            DayOfWeek.SATURDAY  -> "Sáb"
            DayOfWeek.SUNDAY    -> "Dom"
            else                -> "?"
        }
    }

    // ── Mock ──────────────────────────────────────────────────────────────────
    private fun mockWeather(city: String): WeatherInfo {
        // Icono que determina las condiciones simuladas
        val iconCode = "02d"

        // Act 4.2 — Visibilidad dinámica según la condición climática del mock
        val mockVisibility = when (iconCode) {
            "01d", "01n" -> 10  // despejado → 10 km
            "09d", "10d" -> 3   // lluvia → 3 km
            "50d"        -> 1   // niebla → 1 km
            else         -> 8   // default → 8 km
        }

        return WeatherInfo(
            cityName    = city.ifBlank { "Bogotá" },
            country     = "CO",
            temperature = 18.0,
            feelsLike   = 16.5,
            humidity    = 72,
            pressure    = 1013,
            windSpeed   = 14.0,
            windDegree  = 135,  // Act 3.1 — Grados del viento en el mock (equivale a SE)
            visibility  = mockVisibility,
            uvIndex     = 3.0,
            description = "Parcialmente nublado",
            iconCode    = iconCode,
            sunrise     = 1700000000L,
            sunset      = 1700043600L,
            isDay       = true,
            lat         = 4.71,
            lon         = -74.07,
            forecast    = listOf(
                ForecastDay(0, "Hoy", 14.0, 21.0, "Parcialmente nublado", "02d", 72, 14.0, 20),
                ForecastDay(0, "Mar", 13.0, 19.0, "Lluvia ligera",        "10d", 80, 12.0, 65),
                ForecastDay(0, "Mié", 15.0, 23.0, "Cielo despejado",      "01d", 60, 10.0,  5),
                ForecastDay(0, "Jue", 12.0, 17.0, "Tormenta eléctrica",   "11d", 88, 18.0, 80),
                ForecastDay(0, "Vie", 14.0, 20.0, "Muy nublado",          "04d", 75, 13.0, 30),
            )
        )
    }
}
