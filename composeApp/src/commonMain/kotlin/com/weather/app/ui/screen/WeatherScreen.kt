package com.weather.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.weather.app.domain.model.*
import com.weather.app.ui.theme.*

@Composable
fun WeatherScreen(vm: WeatherViewModel) {
    val state by vm.state.collectAsState()
    val query by vm.query.collectAsState()
    val isDay = (state as? WeatherState.Success)?.data?.isDay ?: true

    Box(modifier = Modifier.fillMaxSize()) {
        SkyBg(isDay)
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            SearchBar(
                query        = query,
                onQuery      = vm::onQuery,
                onSearch     = { vm.search() },
                unit         = vm.symbol(),
                onToggleUnit = vm::toggleUnit
            )
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "content"
            ) { s ->
                when (s) {
                    is WeatherState.Idle    -> CenteredMsg("🌤️", "Cargando clima...")
                    is WeatherState.Loading -> LoadingView()
                    is WeatherState.Error   -> ErrorView(s.message) { vm.search("Bogotá") }
                    is WeatherState.Success -> WeatherContent(s.data, vm::convert, vm::symbol)
                }
            }
        }
    }
}

// ── Sky background ────────────────────────────────────────────────────────────
@Composable
private fun SkyBg(isDay: Boolean) {
    val colors = if (isDay)
        listOf(Color(0xFF1B3566), Color(0xFF0A1628), Color(0xFF050C18))
    else
        listOf(Color(0xFF04060F), Color(0xFF080D1C), Color(0xFF050C18))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(colors))
        drawOrb(Offset(size.width * .75f, size.height * .12f), size.width * .4f,
            if (isDay) Color(0x154FC3F7) else Color(0x0A7C3AED))
        drawOrb(Offset(size.width * .1f, size.height * .55f), size.width * .28f, Color(0x0A3B82F6))
    }
}

private fun DrawScope.drawOrb(c: Offset, r: Float, color: Color) =
    drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), c, r), r, c)

// ── Search bar ────────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String, onQuery: (String) -> Unit,
    onSearch: () -> Unit, unit: String, onToggleUnit: () -> Unit
) {
    val kb = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f).height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x1AFFFFFF))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 15.sp)
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = query, onValueChange = onQuery,
                modifier = Modifier.weight(1f), singleLine = true,
                textStyle = TextStyle(color = TextPri, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(); kb?.hide() }),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Buscar ciudad...", color = TextMuted, fontSize = 14.sp)
                    inner()
                }
            )
            if (query.isNotEmpty()) {
                Box(modifier = Modifier.size(28.dp).clickable { onSearch(); kb?.hide() },
                    contentAlignment = Alignment.Center) {
                    Text("↵", color = Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                .background(Color(0x22FFFFFF))
                .border(1.dp, Cyan.copy(alpha = .4f), RoundedCornerShape(14.dp))
                .clickable(onClick = onToggleUnit),
            contentAlignment = Alignment.Center
        ) { Text(unit, color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────
@Composable
private fun WeatherContent(w: WeatherInfo, convert: (Double) -> Int, symbol: () -> String) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HeroTemp(w, convert, symbol) }
        item { StatsCard(w) }
        item { SunCard(w.sunrise, w.sunset) }
        item { ForecastCard(w.forecast, convert, symbol) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────
@Composable
private fun HeroTemp(w: WeatherInfo, convert: (Double) -> Int, symbol: () -> String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${w.cityName}, ${w.country}", color = TextSec, fontSize = 13.sp,
            fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Top) {
            Text("${convert(w.temperature)}", color = TextPri, fontSize = 100.sp,
                fontWeight = FontWeight.Thin, lineHeight = 100.sp)
            Text(symbol(), color = Cyan, fontSize = 32.sp, fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 14.dp))
        }
        Text(w.description, color = TextSec, fontSize = 16.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(4.dp))
        Text("Sensación: ${convert(w.feelsLike)}${symbol()}", color = TextMuted, fontSize = 13.sp)
    }
}

// ── Stats card ────────────────────────────────────────────────────────────────
@Composable
private fun StatsCard(w: WeatherInfo) {
    GlassCard {
        Label("CONDICIONES")
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("💧", "Humedad",  "${w.humidity}%")
            Stat("🌬️", "Viento",  "${w.windSpeed.toInt()} m/s")
            Stat("🔵", "Presión", "${w.pressure} hPa")
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("👁️", "Visib.",   "${w.visibility} km")
            Stat("☀️", "Índice UV","${w.uvIndex.toInt()}")
            // Act 3.1 — Dirección del viento (reemplaza el Stat duplicado de humedad)
            Stat("🧭", "Dir. viento", degreesToCardinal(w.windDegree))
        }
    }
}

@Composable
private fun RowScope.Stat(emoji: String, label: String, value: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

// ── Sun card ──────────────────────────────────────────────────────────────────
@Composable
private fun SunCard(sunrise: Long, sunset: Long) {
    GlassCard {
        Label("SOL")
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            SunItem("🌅", "Amanecer",  fmtTime(sunrise))
            Box(Modifier.width(1.dp).height(36.dp).background(Color(0x22FFFFFF)))
            SunItem("🌇", "Atardecer", fmtTime(sunset))
        }
    }
}

@Composable
private fun SunItem(emoji: String, label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(time,  color = TextPri,   fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

private fun fmtTime(epoch: Long): String {
    val h = ((epoch % 86400) / 3600).toInt()
    val m = ((epoch % 3600)  / 60).toInt()
    return "%02d:%02d".format(h, m)
}

// ── Forecast ──────────────────────────────────────────────────────────────────
@Composable
private fun ForecastCard(days: List<ForecastDay>, convert: (Double) -> Int, symbol: () -> String) {
    GlassCard {
        Label("PRONÓSTICO 5 DÍAS")
        Spacer(Modifier.height(14.dp))
        days.forEachIndexed { i, day ->
            ForecastRow(day, convert, symbol)
            if (i < days.lastIndex)
                HorizontalDivider(color = Color(0x14FFFFFF), modifier = Modifier.padding(vertical = 10.dp))
        }
    }
}

@Composable
private fun ForecastRow(day: ForecastDay, convert: (Double) -> Int, symbol: () -> String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(day.dayLabel, color = TextSec, fontSize = 13.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.width(36.dp))
        Text(weatherEmoji(day.iconCode), fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 10.dp).width(24.dp))
        Column(Modifier.weight(1f)) {
            Text(day.description, color = TextMuted, fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (day.rainChance > 10)
                Text("🌧 ${day.rainChance}%", color = RainBlue, fontSize = 10.sp)
        }
        Text("${convert(day.tempMin)}${symbol()}", color = TextMuted, fontSize = 12.sp,
            modifier = Modifier.width(38.dp), textAlign = TextAlign.End)
        Box(modifier = Modifier.padding(horizontal = 6.dp).width(44.dp).height(3.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Blue, Cyan, Amber))))
        Text("${convert(day.tempMax)}${symbol()}", color = TextPri, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(38.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun Label(text: String) =
    Text(text, color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)

@Composable
private fun CenteredMsg(emoji: String, msg: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(msg, color = TextSec, fontSize = 15.sp)
        }
    }
}

@Composable
private fun LoadingView() {
    val inf = rememberInfiniteTransition(label = "load")
    val alpha by inf.animateFloat(0.25f, 0.65f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alpha")
    Column(Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(.45f, .85f, .65f, .75f).forEach { w ->
            Box(Modifier.fillMaxWidth(w).height(if (w == .85f) 70.dp else 18.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = alpha)))
        }
    }
}

@Composable
private fun ErrorView(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x14FFFFFF))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text(msg, color = TextSec, textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                shape = RoundedCornerShape(12.dp)) {
                Text("Reintentar", color = Navy, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

fun weatherEmoji(code: String) = when {
    code.startsWith("01") -> if (code.endsWith("d")) "☀️" else "🌙"
    code.startsWith("02") -> "⛅"
    code.startsWith("03") -> "🌥️"
    code.startsWith("04") -> "☁️"
    code.startsWith("09") -> "🌧️"
    code.startsWith("10") -> "🌦️"
    code.startsWith("11") -> "⛈️"
    code.startsWith("13") -> "❄️"
    code.startsWith("50") -> "🌫️"
    else -> "🌡️"
}

// Act 3.1 — Convierte grados (0-360) a dirección cardinal del viento
fun degreesToCardinal(deg: Int): String = when (deg) {
    in 0..22,
    in 338..360 -> "N"   // Norte
    in 23..67   -> "NE"  // Noreste
    in 68..112  -> "E"   // Este
    in 113..157 -> "SE"  // Sureste
    in 158..202 -> "S"   // Sur
    in 203..247 -> "SO"  // Suroeste
    in 248..292 -> "O"   // Oeste
    else        -> "NO"  // Noroeste
}
