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
import com.gianlucaparadise.memorandaloco.permission.PermissionsChecker
import com.gianlucaparadise.memorandaloco.permission.PermissionsRequestor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel @ViewModelInject constructor(
    private val geofencingHelper: GeofencingHelper,
    private val permissionsChecker: PermissionsChecker,
    private val permissionsRequestor: PermissionsRequestor,
    private val appDatabase: AppDatabase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val tag = "MainViewModel"

    private val _message = MutableLiveData(MessageDescriptor(type = MessageType.Idle))
    val message: LiveData<MessageDescriptor<MessageType>> = _message

    fun addGeofence() {
        // This is a fire-and-forget style coroutine, therefore I can't raise exception outside here
        viewModelScope.launch {
            try {
                if (!permissionsChecker.hasBackgroundLocationPermission()) throw PermissionsNotGrantedException()

                val home = appDatabase.getHome() ?: throw MissingHomeException()

                geofencingHelper.addGeofence(
                    "HOME",
                    home.location.latitude,
                    home.location.longitude,
                    100f
                )

                _message.value = MessageDescriptor(MessageType.Ok)
            } catch (ex: ApiException) {
                Log.e(tag, "addGeofence: ApiException", ex)

                val errorDescriptor = when (ex.statusCode) {
                    GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                        MessageDescriptor(MessageType.GeofenceNotAvailable, throwable = ex)
                    }
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                        MessageDescriptor(MessageType.GeofenceTooManyGeofences, throwable = ex)
                    }
                    else -> {
                        // generic error
                        MessageDescriptor(
                            MessageType.GenericApiError,
                            code = GeofenceStatusCodes.getStatusCodeString(ex.statusCode),
                            throwable = ex
                        )
                    }
                }

                _message.value = errorDescriptor

            } catch (ex: PermissionsNotGrantedException) {
                Log.e(tag, "addGeofence: PermissionsNotGrantedException", ex)
                _message.value =
                    MessageDescriptor(MessageType.PermissionsNotGranted, throwable = ex)
            } catch (ex: MissingHomeException) {
                Log.e(tag, "addGeofence: MissingHomeException", ex)
                _message.value = MessageDescriptor(MessageType.MissingHome, throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "addGeofence: Error", ex)
                _message.value = MessageDescriptor(MessageType.GenericGeofenceError, throwable = ex)
            }
        }
    }

    fun requestPermissionsAndAddGeofence() {
        viewModelScope.launch {
            try {
                val permissionState =
                    permissionsRequestor.askLocationPermission(bypassRationale = true)

                if (permissionState != PermissionsRequestor.Result.Granted) throw PermissionsNotGrantedException()

                addGeofence()

            } catch (ex: PermissionsNotGrantedException) {
                Log.e(tag, "requestPermissions: PermissionsNotGrantedException", ex)
                _message.value =
                    MessageDescriptor(MessageType.PermissionsNotGranted, throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "requestPermissions: Error", ex)
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
                _message.value = MessageDescriptor(MessageType.InvalidLocationError, throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "requestLocation: error", ex)
                _message.value = MessageDescriptor(MessageType.GenericLocationError, throwable = ex)
            }
        }
    }

    data class MessageDescriptor<T : Enum<T>>(
        val type: T,
        val code: String? = null,
        val throwable: Throwable? = null
    )

    enum class MessageType {
        Idle,
        Ok,
        GeofenceNotAvailable,
        GeofenceTooManyGeofences,
        GenericApiError,
        PermissionsNotGranted,
        MissingHome,
        GenericGeofenceError,
        InvalidLocationError,
        GenericLocationError
    }
}