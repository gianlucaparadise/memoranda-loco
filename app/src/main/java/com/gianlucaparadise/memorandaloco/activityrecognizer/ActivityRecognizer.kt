package com.gianlucaparadise.memorandaloco.activityrecognizer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRecognizer @Inject constructor(@ApplicationContext private val context: Context) {

    private val tag = "ActivityRecognizer"

    private var pendingIntent: PendingIntent? = null // this is evaluated on start

    fun start() {
        val transitions = arrayListOf<ActivityTransition>()
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        val request = ActivityTransitionRequest(transitions)

        val intent = Intent(context, ActivityTransitionUpdateReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(request, pendingIntent)
            .addOnSuccessListener {
                Log.d(tag, "ActivityTransitionUpdates requested")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error while requesting ActivityTransitionUpdates: $e")
            }
    }

    fun stop() {
        val thisPendingIntent = pendingIntent ?: return

        ActivityRecognition.getClient(context)
            .removeActivityTransitionUpdates(thisPendingIntent)
            .addOnSuccessListener {
                thisPendingIntent.cancel()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error while removing ActivityTransitionUpdates: $e")
            }
    }
}