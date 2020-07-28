package com.gianlucaparadise.memorandaloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gianlucaparadise.memorandaloco.activityrecognizer.ActivityRecognizer
import com.gianlucaparadise.memorandaloco.permission.PermissionsHelper
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val activityRecognizer: ActivityRecognizer,
    private val permissionHelper: PermissionsHelper
) : ViewModel() {

    private val tag = "MainViewModel"

    fun startActivityRecognizer() {
        activityRecognizer.start()
    }

    fun askPermissions() {
        viewModelScope.launch {
            val permissionState = permissionHelper.askLocationPermission(bypassRationale = true)
            Log.d(tag, "askPermissions: PermissionState: $permissionState")
        }
    }
}