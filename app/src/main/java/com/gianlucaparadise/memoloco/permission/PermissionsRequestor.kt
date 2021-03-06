package com.gianlucaparadise.memoloco.permission

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ActivityScoped
class PermissionsRequestor @Inject constructor(
    @ActivityContext private val context: Context,
    private val checker: PermissionsChecker
) {

    val canRequestBackgroundPermissions: Boolean
        get() = checker.canRequestBackgroundPermissions

    /**
     * This is used when asking for permission
     */
    private val foregroundLocationPermissions: Array<String>
        get() {
            val result = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            return result.toTypedArray()
        }

    /**
     * This is used when asking for permission
     */
    private val backgroundLocationPermissions: Array<String>
        get() {
            val result = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                result.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            return result.toTypedArray()
        }

    private val tag = "PermissionsHelper"

    enum class Result {
        Granted,
        Denied,
        ShouldRequestPermissionRationale
    }

    /**
     * Request Foreground Location permission ([Manifest.permission.ACCESS_FINE_LOCATION])
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    suspend fun requestForegroundLocationPermission(bypassRationale: Boolean) =
        requestPermission(foregroundLocationPermissions, bypassRationale)

    /**
     * Request Background Location permission. Use [PermissionsChecker.canRequestBackgroundPermissions] to check if you call this method
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    suspend fun requestBackgroundLocationPermission(bypassRationale: Boolean): Result {
        if (!checker.canRequestBackgroundPermissions) {
            throw Exception("You can't request background permissions. Use `canRequestBackgroundPermissions` API to check when you can.")
        }

        return requestPermission(backgroundLocationPermissions, bypassRationale)
    }

    /**
     * Ask for input permission
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    private suspend fun requestPermission(
        permissions: Array<String>,
        bypassRationale: Boolean
    ): Result {
        return suspendCoroutine { continuation ->
            val activity =
                (context as? AppCompatActivity) ?: throw Exception("Context isn't an activity")

            val requestPermissionLauncher =
                activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                    val isGranted = permissionsResult.all { it.value == true }
                    if (isGranted) {
                        Log.d(tag, "askPermission - $permissions: the user just granted")
                        continuation.resume(Result.Granted)
                    } else {
                        Log.d(
                            tag,
                            "askPermission - $permissions: the user didn't grant, feature is not available"
                        )
                        continuation.resume(Result.Denied)
                    }
                }

            when {
                checker.hasPermissions(permissions) -> {
                    Log.d(tag, "askPermission - $permissions: already granted")
                    continuation.resume(Result.Granted)
                }
                shouldShowRequestPermissionsRationale(activity, permissions) -> {
                    Log.d(tag, "askPermission - $permissions: not granted, should show rationale")
                    if (bypassRationale) {
                        requestPermissionLauncher.launch(permissions)
                    } else {
                        continuation.resume(Result.ShouldRequestPermissionRationale)
                    }
                }
                else -> {
                    Log.d(tag, "askPermission - $permissions: not granted, requesting permission")
                    requestPermissionLauncher.launch(permissions)
                }
            }
        }
    }

    private fun shouldShowRequestPermissionsRationale(
        activity: AppCompatActivity,
        permissions: Array<String>
    ): Boolean {
        return permissions.all { shouldShowRequestPermissionRationale(activity, it) }
    }
}