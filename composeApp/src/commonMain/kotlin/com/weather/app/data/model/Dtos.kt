package com.weather.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherDto(
    val coord: CoordDto,
    val weather: List<WeatherDto>,
    val main: MainDto,
    val visibility: Int = 10000,
    val wind: WindDto,
    val sys: SysDto = SysDto(),
    val name: String,
    val dt: Long
)

@Serializable data class CoordDto(val lat: Double, val lon: Double)

@Serializable data class WeatherDto(
    val description: String,
    val icon: String
)

@Serializable data class MainDto(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min")   val tempMin: Double,
    @SerialName("temp_max")   val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

@Serializable data class WindDto(val speed: Double, val deg: Int = 0)

@Serializable data class SysDto(
    val country: String = "",
    val sunrise: Long   = 0,
    val sunset: Long    = 0
)

@Serializable data class ForecastDto(
    val list: List<ForecastItemDto>,
    val city: CityDto
)

@Serializable data class ForecastItemDto(
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDto>,
    val wind: WindDto,
    @SerialName("pop") val pop: Double = 0.0,
    @SerialName("dt_txt") val dtTxt: String
)

@Serializable data class CityDto(
    val name: String,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

@Serializable data class UvDto(val value: Double)
