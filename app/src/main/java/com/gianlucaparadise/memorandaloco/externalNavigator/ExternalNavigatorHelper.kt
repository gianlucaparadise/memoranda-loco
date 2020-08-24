package com.gianlucaparadise.memorandaloco.externalNavigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gianlucaparadise.memorandaloco.vo.LocationDescriptor
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ExternalNavigatorHelper @Inject constructor(@ActivityContext private val context: Context) {

    fun openMapForLocation(location: LocationDescriptor) {
        val uri = "geo:${location.latitude},${location.longitude}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }
}