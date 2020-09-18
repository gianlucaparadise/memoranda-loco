package com.gianlucaparadise.memoloco.externalNavigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gianlucaparadise.memoloco.vo.Place
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ExternalNavigatorHelper @Inject constructor(@ActivityContext private val context: Context) {

    fun openPlaceOnMap(place: Place) {
        val lat = place.location.latitude
        val lon = place.location.longitude
        val label = place.name
        val uri = "geo:0,0?q=$lat,$lon($label)"
        // "label" currently doesn't work, but at least a pin is displayed
        // google maps bug reference: https://issuetracker.google.com/issues/129726279

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }
}