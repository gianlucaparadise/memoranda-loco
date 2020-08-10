package com.gianlucaparadise.memorandaloco.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.text.HtmlCompat
import com.gianlucaparadise.memorandaloco.R
import com.gianlucaparadise.memorandaloco.vo.NotificationAction
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

        Log.d(
            tag,
            "onReceive: Geofence transition received - Type: $geofenceTransition TriggeringGeofence: ${triggeringGeofences.joinToString { it.requestId }}"
        )

        if (!triggeringGeofences.any { it.requestId == "HOME" }) return
        if (context == null) return

        val title: String
        val description: String
        val action: NotificationAction

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                title = context.getString(R.string.notification_title_at_home)
                description = context.getString(R.string.notification_body_at_home)
                action =
                    NotificationAction(
                        NotificationAction.Type.TurnOffBluetooth
                    )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                title = context.getString(R.string.notification_title_outside_home)
                description = context.getString(R.string.notification_body_outside_home)
                action =
                    NotificationAction(
                        NotificationAction.Type.OpenAnotherApp,
                        "it.ministerodellasalute.immuni"
                    )
            }
            else -> {
                // unprocessed geofence transition type
                return
            }
        }

        val descriptionHtml = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        notificationHelper.sendNotification(title, descriptionHtml, action)
    }
}