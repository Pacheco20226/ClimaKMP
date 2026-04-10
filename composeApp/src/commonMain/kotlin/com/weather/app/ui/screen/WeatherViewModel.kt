package com.weather.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.data.api.WeatherApiClient
import com.weather.app.data.repository.WeatherRepository
import com.weather.app.domain.model.TempUnit
import com.weather.app.domain.model.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository(WeatherApiClient())

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _unit = MutableStateFlow(TempUnit.CELSIUS)
    val unit: StateFlow<TempUnit> = _unit.asStateFlow()

    init { loadDefault() }

    fun onQuery(v: String) { _query.value = v }

    fun search(city: String = _query.value.trim()) {
        if (city.isBlank()) return
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            repository.getByCity(city).fold(
                onSuccess = { _state.value = WeatherState.Success(it) },
                onFailure = { _state.value = WeatherState.Error(friendlyError(it)) }
            )
        }
    }

    fun loadByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            repository.getByCoords(lat, lon).fold(
                onSuccess = { _state.value = WeatherState.Success(it) },
                onFailure = { loadDefault() }
            )
        }
    }

    fun loadDefault() {
        // Act 2.2 — Ciudad por defecto cambiada de "Bogotá" a "Medellín"
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            repository.getByCity("Medellín").fold(
                onSuccess = { _state.value = WeatherState.Success(it) },
                onFailure = { _state.value = WeatherState.Error("Activa el GPS o busca una ciudad") }
            )
        }
    }

    fun toggleUnit() {
        _unit.value = if (_unit.value == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS
    }

    fun convert(celsius: Double): Int = when (_unit.value) {
        TempUnit.CELSIUS    -> celsius.toInt()
        TempUnit.FAHRENHEIT -> (celsius * 9 / 5 + 32).toInt()
    }

    fun symbol(): String = if (_unit.value == TempUnit.CELSIUS) "°C" else "°F"

    private fun friendlyError(t: Throwable): String = when {
        t.message?.contains("404") == true                  -> "Ciudad no encontrada"
        t.message?.contains("401") == true                  -> "API key inválida"
        t.message?.contains("UnknownHostException") == true -> "Sin conexión a internet"
        else -> "Error: ${t.message}"
    }
}
