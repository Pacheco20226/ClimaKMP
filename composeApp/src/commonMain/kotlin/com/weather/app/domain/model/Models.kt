package com.weather.app.domain.model

data class WeatherInfo(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDegree: Int,      // Act 3.1 — Dirección del viento en grados (0-360)
    val visibility: Int,
    val uvIndex: Double,
    val description: String,
    val iconCode: String,
    val sunrise: Long,
    val sunset: Long,
    val isDay: Boolean,
    val lat: Double,
    val lon: Double,
    val forecast: List<ForecastDay>
)

data class ForecastDay(
    val date: Long,
    val dayLabel: String,
    val tempMin: Double,
    val tempMax: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val rainChance: Int
)

sealed class WeatherState {
    data object Idle    : WeatherState()
    data object Loading : WeatherState()
    data class Success(val data: WeatherInfo) : WeatherState()
    data class Error(val message: String)     : WeatherState()
}

enum class TempUnit { CELSIUS, FAHRENHEIT }
