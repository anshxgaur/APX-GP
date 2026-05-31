package com.yourname.sra.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onFailure("Location permission not granted")
            return
        }

        try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    // Fallback to last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            onSuccess(lastLocation.latitude, lastLocation.longitude)
                        } else {
                            onFailure("Unable to get current location. Please ensure GPS is enabled.")
                        }
                    }.addOnFailureListener {
                        onFailure("Unable to get location: ${it.localizedMessage}")
                    }
                }
            }.addOnFailureListener {
                onFailure("Location error: ${it.localizedMessage}")
            }
        } catch (e: SecurityException) {
            onFailure("Location permission denied")
        }
    }

    @Suppress("DEPRECATION")
    suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var result = ""
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            result = buildString {
                                address.getAddressLine(0)?.let { append(it) }
                            }
                        }
                    }
                    // Give geocoder time to callback
                    kotlinx.coroutines.delay(500)
                    result.ifEmpty { "Lat: $lat, Lng: $lng" }
                } else {
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        addresses[0].getAddressLine(0) ?: "Lat: $lat, Lng: $lng"
                    } else {
                        "Lat: $lat, Lng: $lng"
                    }
                }
            } catch (e: Exception) {
                "Lat: $lat, Lng: $lng"
            }
        }
    }
}
