package com.weather.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weather.app.ui.screen.WeatherScreen
import com.weather.app.ui.screen.WeatherViewModel
import com.weather.app.ui.theme.WeatherTheme

@Composable
fun App() {
    WeatherTheme {
        // viewModel() crea y cachea la instancia correctamente en Compose
        val vm: WeatherViewModel = viewModel { WeatherViewModel() }
        WeatherScreen(vm = vm)
    }
}
