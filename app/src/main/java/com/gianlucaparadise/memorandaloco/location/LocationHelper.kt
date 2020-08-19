package com.gianlucaparadise.memorandaloco.location

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gianlucaparadise.memorandaloco.exception.InvalidLocationException
import com.gianlucaparadise.memorandaloco.exception.PermissionsNotGrantedException
import com.gianlucaparadise.memorandaloco.vo.LocationDescriptor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ActivityScoped
class LocationHelper @Inject constructor(@ActivityContext context: Context) {

    private val tag = "LocationHelper"

    private var fusedLocationClient: FusedLocationProviderClient

    init {
        val activity =
            (context as? AppCompatActivity) ?: throw Exception("Context isn't an activity")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    suspend fun getCurrentLocation(): LocationDescriptor {
        // TODO: when location is null or old, request location explicitly
        val location = getLastLocation() ?: throw InvalidLocationException()
        return LocationDescriptor(location.latitude, location.longitude)
    }

    private suspend fun getLastLocation(): Location? {
        return suspendCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        continuation.resume(location)
                    }
                    .addOnFailureListener { ex ->
                        Log.e(tag, "getLastLocation: error while reading last location", ex)
                        continuation.resumeWithException(ex)
                    }
            } catch (ex: SecurityException) {
                Log.e(tag, "getLastLocation: SecurityException", ex)
                continuation.resumeWithException(PermissionsNotGrantedException())
            } catch (ex: Exception) {
                Log.e(tag, "getLastLocation: Exception", ex)
                continuation.resumeWithException(ex)
            }
        }
    }
}