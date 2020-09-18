package com.gianlucaparadise.memoloco.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gianlucaparadise.memoloco.exception.GpsTurnedOffException
import com.gianlucaparadise.memoloco.exception.InvalidLocationException
import com.gianlucaparadise.memoloco.exception.PermissionsNotGrantedException
import com.gianlucaparadise.memoloco.vo.LocationDescriptor
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ActivityScoped
class LocationHelper @Inject constructor(@ActivityContext val context: Context) {

    private val tag = "LocationHelper"

    private var fusedLocationClient: FusedLocationProviderClient

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val activity: AppCompatActivity
        get() = (context as? AppCompatActivity) ?: throw Exception("Context isn't an activity")

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    private val isGpsEnabled: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    @Throws(InvalidLocationException::class, GpsTurnedOffException::class)
    suspend fun getCurrentLocation(): LocationDescriptor {
        checkGpsState()

        val location = getLastLocation() ?: throw InvalidLocationException("Location is null")

        if (location.accuracy >= 100) {
            throw InvalidLocationException("Location accuracy is too low: ${location.accuracy}")
        }

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

    @Throws(GpsTurnedOffException::class)
    private fun checkGpsState() {
        if (isGpsEnabled) return
        throw GpsTurnedOffException()
    }

    /**
     * When GPS is disabled, this asks the user to turn it on
     */
    suspend fun askToTurnOnGpsIfNeeded() {
        if (isGpsEnabled) return

        return suspendCoroutine { continuation ->
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(activity)
            client.checkLocationSettings(builder.build())
                .addOnSuccessListener { locationSettingsResponse ->
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    Log.d(tag, "askToTurnOnGpsIfNeeded: Success $locationSettingsResponse")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "askToTurnOnGpsIfNeeded: Failure", exception)
                    if (exception is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().

                            val launcher =
                                activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                                    if (result.resultCode == Activity.RESULT_OK) {
                                        continuation.resume(Unit)
                                    } else {
                                        continuation.resumeWithException(GpsTurnedOffException())
                                    }
                                }

                            val intentSenderRequest = IntentSenderRequest
                                .Builder(exception.resolution)
                                .build()
                            launcher.launch(intentSenderRequest)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                            Log.d(tag, "askToTurnOnGpsIfNeeded: Error to be ignored", sendEx)
                        }
                    } else {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }
}