package com.gianlucaparadise.memoloco.ui.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.gianlucaparadise.memoloco.alert.AlertHelper
import com.gianlucaparadise.memoloco.constants.OFFICIAL_CONTACT_TRACING_APPS
import com.gianlucaparadise.memoloco.db.AppDatabase
import com.gianlucaparadise.memoloco.exception.*
import com.gianlucaparadise.memoloco.externalNavigator.ExternalNavigatorHelper
import com.gianlucaparadise.memoloco.geofencing.GeofencingHelper
import com.gianlucaparadise.memoloco.location.LocationHelper
import com.gianlucaparadise.memoloco.permission.PermissionsChecker
import com.gianlucaparadise.memoloco.permission.PermissionsRequestor
import com.gianlucaparadise.memoloco.preference.PreferenceHelper
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

    val selectedApp = MutableLiveData<String?>()

    private val _canChooseApp = MediatorLiveData<Boolean>()
    val canChooseApp: LiveData<Boolean> = _canChooseApp

    private val _isLocationPermissionsDialogVisible = MutableLiveData<Boolean>(false)
    val isLocationPermissionsDialogVisible : LiveData<Boolean> = _isLocationPermissionsDialogVisible

    private val homeGeofenceId = "HOME"

    init {
        _canChooseApp.addSource(selectedApp) {
            _canChooseApp.value = !it.isNullOrBlank()
        }
    }

    fun addGeofence() {
        // This is a fire-and-forget style coroutine, therefore I can't raise exception outside here
        viewModelScope.launch {
            try {
                if (!permissionsChecker.hasBackgroundLocationPermission()) throw PermissionsNotGrantedException()

                val home = appDatabase.getHome() ?: throw MissingHomeException()

                if (preferenceHelper.appToOpen == null) throw MissingAppToOpenException()
                // `AppToOpen` will be used by GeofencingUpdateReceiver when the notification is created

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
                    MessageDescriptor(getMessageTypeDependingOnPermissionState(), throwable = ex)
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

    fun startPermissionsRequestAndAddGeofence() {
        _isLocationPermissionsDialogVisible.value = true
    }

    fun onLocationPermissionsDialogCancel() {
        _isLocationPermissionsDialogVisible.value = false
    }

    fun onLocationPermissionsDialogConfirm() {
        _isLocationPermissionsDialogVisible.value = false
        requestPermissionsAndAddGeofence()
    }

    private fun requestPermissionsAndAddGeofence() {
        viewModelScope.launch {
            try {
                val permissionState = with(permissionsRequestor) {
                    if (canRequestBackgroundPermissions) requestBackgroundLocationPermission(bypassRationale = true)
                    else requestForegroundLocationPermission(bypassRationale = true)
                }

                if (permissionState != PermissionsRequestor.Result.Granted) throw PermissionsNotGrantedException()

                val needsAlsoBackgroundLocationPermissions = !permissionsChecker.hasBackgroundLocationPermission()
                if(needsAlsoBackgroundLocationPermissions) {
                    _message.value = MessageDescriptor(MessageType.BackgroundPermissionsNotGranted)
                    return@launch
                }

                addGeofence()

            } catch (ex: PermissionsNotGrantedException) {
                Log.e(tag, "requestPermissions: PermissionsNotGrantedException", ex)
                _message.value =
                    MessageDescriptor(getMessageTypeDependingOnPermissionState(), throwable = ex)
            } catch (ex: Exception) {
                Log.e(tag, "requestPermissions: Error", ex)
            }
        }
    }

    private fun getMessageTypeDependingOnPermissionState() : MessageType {
        return permissionsChecker.run {
            when {
                needsTwoStepsPermissionRequest && !canRequestBackgroundPermissions -> MessageType.TwoStepsPermissionRequestNeeded
                needsTwoStepsPermissionRequest && canRequestBackgroundPermissions -> MessageType.BackgroundPermissionsNotGranted
                !needsTwoStepsPermissionRequest -> MessageType.PermissionsNotGranted
                else -> throw Exception("Invalid operation") // I should never fall here
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
                preferenceHelper.appToOpen = null

                addGeofence() // this will fail because HomePlace is missing
            } catch (ex: Exception) {
                Log.e(tag, "removeGeofence: Error", ex)
                _message.value =
                    MessageDescriptor(MessageType.GenericRemoveGeofenceError, throwable = ex)
            }
        }
    }

    fun chooseAppAndAddGeofence() {
        try {
            val appToOpen = selectedApp.value
            if (appToOpen.isNullOrBlank()) throw MissingAppToOpenException()

            preferenceHelper.appToOpen = appToOpen

            addGeofence()
        } catch (ex: MissingAppToOpenException) {
            Log.e(tag, "chooseAppAndAddGeofence: MissingAppToOpenException", ex)
            _message.value = MessageDescriptor(MessageType.MissingAppToOpen, throwable = ex)
        }
    }

    /**
     * Preselects the first contact tracing app found
     * @param installedApps List of the package names of the apps installed on the phone
     */
    fun preselectApp(installedApps: List<String>) {
        val appsToPreselect = OFFICIAL_CONTACT_TRACING_APPS
        val installedContactTracingApps = appsToPreselect.intersect(installedApps)

        Log.d(tag, "preselectApp: installedContactTracingApps: $installedContactTracingApps")
        val appToChoose = installedContactTracingApps.firstOrNull()
        if (appToChoose.isNullOrBlank()) return

        this.selectedApp.value = appToChoose
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
        BackgroundPermissionsNotGranted,
        TwoStepsPermissionRequestNeeded,
        MissingHome,
        GenericAddGeofenceError,
        GenericRemoveGeofenceError,
        InvalidLocationError,
        GenericLocationError,
        MissingAppToOpen
    }
}