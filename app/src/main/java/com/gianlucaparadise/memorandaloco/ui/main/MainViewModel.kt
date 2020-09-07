package com.gianlucaparadise.memorandaloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gianlucaparadise.memorandaloco.alert.AlertHelper
import com.gianlucaparadise.memorandaloco.db.AppDatabase
import com.gianlucaparadise.memorandaloco.exception.*
import com.gianlucaparadise.memorandaloco.externalNavigator.ExternalNavigatorHelper
import com.gianlucaparadise.memorandaloco.geofencing.GeofencingHelper
import com.gianlucaparadise.memorandaloco.location.LocationHelper
import com.gianlucaparadise.memorandaloco.permission.PermissionsChecker
import com.gianlucaparadise.memorandaloco.permission.PermissionsRequestor
import com.gianlucaparadise.memorandaloco.preference.PreferenceHelper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel @ViewModelInject constructor(
    private val geofencingHelper: GeofencingHelper,
    private val permissionsChecker: PermissionsChecker,
    private val permissionsRequestor: PermissionsRequestor,
    private val appDatabase: AppDatabase,
    private val locationHelper: LocationHelper,
    private val externalNavigator: ExternalNavigatorHelper,
    private val alertHelper: AlertHelper,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val tag = "MainViewModel"

    private val _message = MutableLiveData(MessageDescriptor(type = MessageType.Idle))
    val message: LiveData<MessageDescriptor<MessageType>> = _message

    val selectedApp: MutableLiveData<String?> = MutableLiveData("it.ministerodellasalute.immuni")

    private val homeGeofenceId = "HOME"

    fun addGeofence() {
        // This is a fire-and-forget style coroutine, therefore I can't raise exception outside here
        viewModelScope.launch {
            try {
                if (!permissionsChecker.hasBackgroundLocationPermission()) throw PermissionsNotGrantedException()

                val home = appDatabase.getHome() ?: throw MissingHomeException()

                val appToOpen = preferenceHelper.appToOpen ?: throw MissingAppToOpenException()

                geofencingHelper.addGeofence(
                    homeGeofenceId,
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
            } catch (ex: MissingAppToOpenException) {
                Log.e(tag, "addGeofence: MissingAppToOpenException", ex)
                _message.value = MessageDescriptor(MessageType.MissingAppToOpen, throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "addGeofence: Error", ex)
                _message.value =
                    MessageDescriptor(MessageType.GenericAddGeofenceError, throwable = ex)
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
            requestLocationAndAddGeofenceAsync()
        }
    }

    fun updateHomeAndAddGeofence() {
        viewModelScope.launch {
            requestLocationAndAddGeofenceAsync()
            alertHelper.showSnackbar(AlertHelper.MessageType.HomeUpdated)
        }
    }

    private suspend fun requestLocationAndAddGeofenceAsync() {
        try {
            locationHelper.askToTurnOnGpsIfNeeded()

            val currentLocation = locationHelper.getCurrentLocation()
            Log.d(tag, "requestLocation: $currentLocation")

            val label = "Home" // TODO: create a StringProvider to localize this string
            appDatabase.saveHome(currentLocation, label)

            addGeofence()

        } catch (ex: InvalidLocationException) {
            Log.e(tag, "requestLocation: InvalidLocationException", ex)
            _message.value = MessageDescriptor(MessageType.InvalidLocationError, throwable = ex)
        } catch (ex: GpsTurnedOffException) {
            Log.e(tag, "requestLocation: GpsTurnedOffException", ex)
            alertHelper.showSnackbar(AlertHelper.MessageType.GpsTurnedOffError)
        } catch (ex: Exception) {
            Log.e(tag, "requestLocation: error", ex)
            _message.value = MessageDescriptor(MessageType.GenericLocationError, throwable = ex)
        }
    }

    fun checkHome() {
        viewModelScope.launch {
            try {
                val home = appDatabase.getHome() ?: throw MissingHomeException()
                externalNavigator.openPlaceOnMap(home)

            } catch (ex: MissingHomeException) {
                Log.e(tag, "checkHome: MissingHomeException", ex)
                _message.value = MessageDescriptor(MessageType.MissingHome, throwable = ex)
            }
        }
    }

    fun removeGeofence() {
        viewModelScope.launch {
            try {
                geofencingHelper.removeGeofences(homeGeofenceId)
                appDatabase.deleteHomeIfPresent()

                addGeofence() // this will fail because HomePlace is missing
            } catch (ex: Exception) {
                Log.e(tag, "removeGeofence: Error", ex)
                _message.value =
                    MessageDescriptor(MessageType.GenericRemoveGeofenceError, throwable = ex)
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
        GenericAddGeofenceError,
        GenericRemoveGeofenceError,
        InvalidLocationError,
        GenericLocationError,
        MissingAppToOpen
    }
}