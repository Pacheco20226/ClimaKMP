package com.weather.app.data.api

import com.weather.app.data.model.CurrentWeatherDto
import com.weather.app.data.model.ForecastDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WeatherApiClient {

    companion object {
        // ⚠️ Reemplaza con tu key de openweathermap.org
        const val API_KEY = "YOUR_API_KEY_HERE"
        private const val BASE = "https://api.openweathermap.org/data/2.5"
    }

    // HttpClient sin engine explícito — cada plataforma usa el suyo automáticamente
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    suspend fun currentByCity(city: String): CurrentWeatherDto =
        client.get("$BASE/weather?q=$city&appid=$API_KEY&units=metric&lang=es").body()

    suspend fun currentByCoords(lat: Double, lon: Double): CurrentWeatherDto =
        client.get("$BASE/weather?lat=$lat&lon=$lon&appid=$API_KEY&units=metric&lang=es").body()

    suspend fun forecastByCity(city: String): ForecastDto =
        client.get("$BASE/forecast?q=$city&appid=$API_KEY&units=metric&lang=es&cnt=40").body()

    suspend fun forecastByCoords(lat: Double, lon: Double): ForecastDto =
        client.get("$BASE/forecast?lat=$lat&lon=$lon&appid=$API_KEY&units=metric&lang=es&cnt=40").body()
}
