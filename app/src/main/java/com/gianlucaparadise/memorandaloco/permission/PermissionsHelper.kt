package com.gianlucaparadise.memorandaloco.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ActivityScoped
class PermissionsHelper @Inject constructor(@ActivityContext private val context: Context) {

    companion object {
        fun hasLocationPermission(context: Context) =
            hasPermission(context, backgroundLocationPermission)

        private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
            return permissions.all { hasPermission(context, it) }
        }

        private fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * This is used when asking for permission
         */
        private val locationPermissions: Array<String>
            get() {
                val result = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    result.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                return result.toTypedArray()
            }

        /**
         * This is used to check that the user has given background location permission
         */
        private val backgroundLocationPermission: String
            get() {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                } else {
                    Manifest.permission.ACCESS_FINE_LOCATION
                }
            }
    }

    private val tag = "PermissionsHelper"

    enum class Result {
        Granted,
        Denied,
        ShouldRequestPermissionRationale
    }

    /**
     * Ask for Location permission
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    suspend fun askLocationPermission(bypassRationale: Boolean) =
        askPermission(locationPermissions, bypassRationale)

    /**
     * Ask for input permission
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    private suspend fun askPermission(
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
                hasPermissions(context, permissions) -> {
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