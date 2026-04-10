# WeatherApp KMP

App de clima para Android con Kotlin Multiplatform.

## Tecnologías
- Kotlin 2.1.0 + Compose Multiplatform 1.7.0
- Ktor 2.3.12 — cliente HTTP
- OpenWeatherMap API

## Cambios realizados

### Actividad 2 — Personalización de la Interfaz
1. **Color principal (Act 2.1):** El color `Cyan` fue cambiado de `0xFF4DD9E8` (cian) a `0xFFB983FF` (violeta). Archivo modificado: `composeApp/src/commonMain/kotlin/com/weather/app/ui/theme/Theme.kt`
2. **Ciudad por defecto (Act 2.2):** La función `loadDefault()` ahora carga **Medellín** en lugar de Bogotá. Archivo modificado: `composeApp/src/commonMain/kotlin/com/weather/app/ui/screen/WeatherViewModel.kt`

### Actividad 3 — Extender la Funcionalidad
3. **Dirección del viento (Act 3.1):** Se agregó el campo `windDegree: Int` al modelo `WeatherInfo`, se mapeó desde `c.wind.deg` en el repositorio y se añadió la función `degreesToCardinal()` en `WeatherScreen.kt`. El `StatsCard` ahora muestra la dirección cardinal del viento (N, NE, E, SE, S, SO, O, NO) en lugar del stat duplicado de humedad.

### Actividad 4 — Explorar los Datos de la API
4. **Visibilidad dinámica en mock (Act 4.2):** La función `mockWeather()` en `WeatherRepository.kt` ahora calcula la visibilidad según el `iconCode`: despejado → 10 km, lluvia → 3 km, niebla → 1 km, otros → 8 km.

### Actividad 5 — Control de Versiones
5. **`.gitignore` creado** para excluir carpetas `build/`, `.gradle/`, `.idea/`, `local.properties` y `secrets.properties`.

## Seguridad
La API key **no está incluida** en este repositorio. Para usar la app con datos reales, reemplaza `YOUR_API_KEY_HERE` en `WeatherApiClient.kt` con tu clave de [OpenWeatherMap](https://openweathermap.org).
