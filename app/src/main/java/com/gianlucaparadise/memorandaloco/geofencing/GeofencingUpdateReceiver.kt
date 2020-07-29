package com.gianlucaparadise.memorandaloco.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gianlucaparadise.memorandaloco.notification.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Since Hilt is in alpha, doesn't support very well BroadcastReceivers
// This is a workaround taken from: https://github.com/google/dagger/issues/1918#issuecomment-644239233
@AndroidEntryPoint(BroadcastReceiver::class)
class GeofencingUpdateReceiver : Hilt_GeofencingUpdateReceiver() {

    private val tag = "GeofencingUpdateReceive"

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent) // injection happens here

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(tag, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Get the geofences that were triggered. A single event can trigger
        // multiple geofences.
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        val geofencesString = triggeringGeofences.joinToString(",") { it.requestId }
        val resultString = "TransitionType:$geofenceTransition geofences[$geofencesString]"

        notificationHelper.sendNotification(
            "Geofencing Update received",
            resultString
        )
    }
}