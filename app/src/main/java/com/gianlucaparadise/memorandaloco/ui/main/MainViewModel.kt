package com.gianlucaparadise.memorandaloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gianlucaparadise.memorandaloco.geofencing.GeofencingHelper
import com.gianlucaparadise.memorandaloco.permission.PermissionsHelper
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val geofencingHelper: GeofencingHelper,
    private val permissionHelper: PermissionsHelper
) : ViewModel() {

    private val tag = "MainViewModel"

    fun addGeofence() {
        viewModelScope.launch {
            val permissionState = permissionHelper.askLocationPermission(bypassRationale = true)
            Log.d(tag, "askPermissions: PermissionState: $permissionState")
            geofencingHelper.addGeofence("HOME", 0.0, 0.0, 100f)
        }
    }
}