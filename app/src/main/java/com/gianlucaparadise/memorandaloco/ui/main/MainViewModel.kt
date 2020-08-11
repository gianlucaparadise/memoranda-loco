package com.gianlucaparadise.memorandaloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gianlucaparadise.memorandaloco.exception.PermissionsNotGrantedException
import com.gianlucaparadise.memorandaloco.geofencing.GeofencingHelper
import com.gianlucaparadise.memorandaloco.permission.PermissionsChecker
import com.gianlucaparadise.memorandaloco.permission.PermissionsRequestor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel @ViewModelInject constructor(
    private val geofencingHelper: GeofencingHelper,
    private val permissionsChecker: PermissionsChecker,
    private val permissionsRequestor: PermissionsRequestor
) : ViewModel() {

    private val tag = "MainViewModel"

    private val _geofenceError = MutableLiveData<ErrorDescriptor<ErrorType>>()
    val geofenceError: LiveData<ErrorDescriptor<ErrorType>> = _geofenceError

    fun checkPermissions() {
        permissionsChecker.hasBackgroundLocationPermission()
    }

    fun addGeofence() {
        // This is a fire-and-forget style coroutine, therefore I can't raise exception outside here
        viewModelScope.launch {
            try {
                val permissionState =
                    permissionsRequestor.askLocationPermission(bypassRationale = true)
                Log.d(tag, "askPermissions: PermissionState: $permissionState")

                geofencingHelper.addGeofence("HOME", 45.444055, 9.225502, 100f)

                _geofenceError.value = ErrorDescriptor(ErrorType.None)
            } catch (ex: Exception) {
                val errorDescriptor = when (ex) {
                    is ApiException -> when (ex.statusCode) {
                        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                            ErrorDescriptor(ErrorType.GeofenceNotAvailable, throwable = ex)
                        }
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                            ErrorDescriptor(ErrorType.GeofenceTooManyGeofences, throwable = ex)
                        }
                        else -> {
                            // generic error
                            ErrorDescriptor(
                                ErrorType.GenericApiError,
                                code = GeofenceStatusCodes.getStatusCodeString(ex.statusCode),
                                throwable = ex
                            )
                        }
                    }
                    is PermissionsNotGrantedException -> {
                        ErrorDescriptor(ErrorType.PermissionsNotGranted, throwable = ex)
                    }
                    else -> {
                        ErrorDescriptor(ErrorType.GenericError, throwable = ex)
                    }
                }

                _geofenceError.value = errorDescriptor
            }
        }
    }

    data class ErrorDescriptor<T : Enum<T>>(
        val type: T,
        val code: String? = null,
        val throwable: Throwable? = null
    )

    enum class ErrorType {
        None,
        GeofenceNotAvailable,
        GeofenceTooManyGeofences,
        GenericApiError,
        PermissionsNotGranted,
        GenericError,
    }
}