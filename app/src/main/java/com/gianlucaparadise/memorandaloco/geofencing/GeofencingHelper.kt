package com.gianlucaparadise.memorandaloco.geofencing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gianlucaparadise.memorandaloco.permission.PermissionsHelper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofencingHelper @Inject constructor(@ApplicationContext val context: Context) {

    private val tag = "GeofencingHelper"

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private fun buildGeofenceObject(id: String, latitude: Double, longitude: Double, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                latitude, longitude, radius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setLoiteringDelay(300000) // 5 minutes
            .build()
    }

    private fun buildGeofencingRequest(id: String, latitude: Double, longitude: Double, radius: Float): GeofencingRequest {
        val geofenceList = listOf(buildGeofenceObject(id, latitude, longitude, radius))
        return GeofencingRequest.Builder().apply {
            // Specifying INITIAL_TRIGGER_ENTER tells Location services that GEOFENCE_TRANSITION_ENTER should be triggered if the device is already inside the geofence.
            // setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun buildGeofencingPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofencingUpdateReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun addGeofence(id: String, latitude: Double, longitude: Double, radius: Float) {
        if (!PermissionsHelper.hasLocationPermission(context)) return

        try {
            geofencingClient.addGeofences(buildGeofencingRequest(id, latitude, longitude, radius), buildGeofencingPendingIntent())
                .addOnSuccessListener {
                    Log.d(tag, "Geofence added")
                }
                .addOnFailureListener {
                    val ex = it as? ApiException
                    if (ex?.statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                        throw Exception("Geofence not available", it)
                        // To enable geofence:
                        // - Settings▸Security&Location▸Location▸Mode and choose High accuracy
                        // - Settings▸Security&Location▸Location▸Google Location Accuracy and choose Improve Location Accuracy
                    }

                    Log.e(tag, "Geofence adding failed", it)
                }
        } catch (ex: SecurityException) {
            Log.e(tag, "GeofencingClient failed while adding", ex)
        }
    }
}