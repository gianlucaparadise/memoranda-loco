package com.gianlucaparadise.memorandaloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gianlucaparadise.memorandaloco.db.AppDatabase
import com.gianlucaparadise.memorandaloco.exception.InvalidLocationException
import com.gianlucaparadise.memorandaloco.exception.MissingHomeException
import com.gianlucaparadise.memorandaloco.exception.PermissionsNotGrantedException
import com.gianlucaparadise.memorandaloco.geofencing.GeofencingHelper
import com.gianlucaparadise.memorandaloco.location.LocationHelper
import com.gianlucaparadise.memorandaloco.permission.PermissionsRequestor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel @ViewModelInject constructor(
    private val geofencingHelper: GeofencingHelper,
    private val permissionsRequestor: PermissionsRequestor,
    private val appDatabase: AppDatabase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val tag = "MainViewModel"

    private val _geofenceError = MutableLiveData<ErrorDescriptor<ErrorType>>()
    val geofenceError: LiveData<ErrorDescriptor<ErrorType>> = _geofenceError

    fun addGeofence() {
        // This is a fire-and-forget style coroutine, therefore I can't raise exception outside here
        viewModelScope.launch {
            try {
                val permissionState =
                    permissionsRequestor.askLocationPermission(bypassRationale = true)
                Log.d(tag, "askPermissions: PermissionState: $permissionState")

                val home = appDatabase.getHome() ?: throw MissingHomeException()

                geofencingHelper.addGeofence(
                    "HOME",
                    home.location.latitude,
                    home.location.longitude,
                    100f
                )

                _geofenceError.value = ErrorDescriptor(ErrorType.None)
            } catch (ex: Exception) {
                Log.e(tag, "addGeofence: Error", ex)

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
                    is MissingHomeException -> {
                        ErrorDescriptor(ErrorType.MissingHome, throwable = ex)
                    }
                    else -> {
                        ErrorDescriptor(ErrorType.GenericError, throwable = ex)
                    }
                }

                _geofenceError.value = errorDescriptor
            }
        }
    }

    fun requestLocationAndAddGeofence() {
        viewModelScope.launch {
            try {
                val currentLocation = locationHelper.getCurrentLocation()
                Log.d(tag, "requestLocation: $currentLocation")

                appDatabase.saveHome(currentLocation)

                addGeofence()

            } catch (ex: InvalidLocationException) {
                Log.e(tag, "requestLocation: InvalidLocationException", ex)
                _geofenceError.value =
                    ErrorDescriptor(ErrorType.InvalidLocationError, throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "requestLocation: error", ex)
                _geofenceError.value =
                    ErrorDescriptor(ErrorType.GenericLocationError, throwable = ex)
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
        MissingHome,
        GenericError,
        InvalidLocationError,
        GenericLocationError
    }
}