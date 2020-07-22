package com.gianlucaparadise.memorandaloco.activityrecognizer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransitionResult

class ActivityTransitionUpdateReceiver : BroadcastReceiver() {
    private val tag = "ActivityTransitionUpdat"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(tag, "onReceive")

        if (!ActivityTransitionResult.hasResult(intent)) return
        val result = ActivityTransitionResult.extractResult(intent) ?: return

        for (transitionEvent in result.transitionEvents) {
            Log.d(tag, "$transitionEvent")
        }
    }
}