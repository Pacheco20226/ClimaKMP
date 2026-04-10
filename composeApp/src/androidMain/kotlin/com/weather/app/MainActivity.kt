package com.weather.app

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weather.app.ui.screen.WeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {

    private val vm: WeatherViewModel by lazy {
        ViewModelProvider(this)[WeatherViewModel::class.java]
    }

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            fetchLocationAndLoad()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        permissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        setContent { App() }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndLoad() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        CoroutineScope(Dispatchers.IO).launch {
            val location = suspendCancellableCoroutine { cont ->
                val cts = CancellationTokenSource()
                cont.invokeOnCancellation { cts.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (location != null) {
                vm.loadByCoords(location.latitude, location.longitude)
            }
        }
    }
}
