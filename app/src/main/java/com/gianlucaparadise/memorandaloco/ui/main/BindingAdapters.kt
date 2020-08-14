package com.gianlucaparadise.memorandaloco.ui.main

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.gianlucaparadise.memorandaloco.R

@BindingAdapter("geofenceError")
fun bindGeofenceErrorDescriptor(
    view: TextView,
    errorDescriptor: MainViewModel.ErrorDescriptor<MainViewModel.ErrorType>?
) {
    if (errorDescriptor == null) return

    val errorText = when (errorDescriptor.type) {
        MainViewModel.ErrorType.None -> view.context.getString(R.string.geofence_ok)
        MainViewModel.ErrorType.GeofenceNotAvailable -> view.context.getString(R.string.error_geofence_not_available)
        MainViewModel.ErrorType.GeofenceTooManyGeofences -> view.context.getString(R.string.error_too_many_geofences)
        MainViewModel.ErrorType.GenericApiError -> view.context.getString(
            R.string.error_geofences_generic_with_code,
            errorDescriptor.code
        )
        MainViewModel.ErrorType.PermissionsNotGranted -> view.context.getString(R.string.error_permissions_not_granted)
        MainViewModel.ErrorType.MissingHome -> view.context.getString(R.string.error_missing_home)
        MainViewModel.ErrorType.GenericError -> view.context.getString(R.string.error_geofences_generic)
    }

    view.text = errorText
}