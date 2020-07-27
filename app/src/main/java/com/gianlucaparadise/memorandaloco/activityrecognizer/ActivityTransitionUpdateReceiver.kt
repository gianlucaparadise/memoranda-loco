package com.gianlucaparadise.memorandaloco.activityrecognizer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gianlucaparadise.memorandaloco.notification.NotificationHelper
import com.google.android.gms.location.ActivityTransitionResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Since Hilt is in alpha, doesn't support very well BroadcastReceivers
// This is a workaround taken from: https://github.com/google/dagger/issues/1918#issuecomment-644239233
@AndroidEntryPoint(BroadcastReceiver::class)
class ActivityTransitionUpdateReceiver : Hilt_ActivityTransitionUpdateReceiver() {
    private val tag = "ActivityTransitionUpdat"

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent) // injection happens here

        Log.d(tag, "onReceive")

        if (!ActivityTransitionResult.hasResult(intent)) {
            // This is a test notification
            notificationHelper.sendNotification(
                "Activity Transition Update received",
                "no result found"
            )
            return
        }

        val result = ActivityTransitionResult.extractResult(intent) ?: return

        for (transitionEvent in result.transitionEvents) {
            Log.d(tag, "$transitionEvent")
        }

        val resultString =
            result.transitionEvents.joinToString("\n") { "activity:${it.activityType}:transition:${it.transitionType}" }

        notificationHelper.sendNotification(
            "Activity Transition Update received",
            "${result.transitionEvents.size} results received\n $resultString"
        )
    }
}