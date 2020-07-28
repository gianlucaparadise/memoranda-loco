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
        fun hasLocationPermission(context: Context) = hasPermission(context, locationPermission)

        private fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        private val locationPermission: String
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
        askPermission(locationPermission, bypassRationale)

    /**
     * Ask for input permission
     * @param bypassRationale When true, the permissions are asked even if the app should show request permission rationale
     */
    private suspend fun askPermission(permission: String, bypassRationale: Boolean): Result {
        return suspendCoroutine { continuation ->
            val activity =
                (context as? AppCompatActivity) ?: throw Exception("Context isn't an activity")

            val requestPermissionLauncher =
                activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        Log.d(tag, "askPermission - $permission: the user just granted")
                        continuation.resume(Result.Granted)
                    } else {
                        Log.d(
                            tag,
                            "askPermission - $permission: the user didn't grant, feature is not available"
                        )
                        continuation.resume(Result.Denied)
                    }
                }

            when {
                hasPermission(context, permission) -> {
                    Log.d(tag, "askPermission - $permission: already granted")
                    continuation.resume(Result.Granted)
                }
                shouldShowRequestPermissionRationale(activity, permission) -> {
                    Log.d(tag, "askPermission - $permission: not granted, should show rationale")
                    if (bypassRationale) {
                        requestPermissionLauncher.launch(permission)
                    } else {
                        continuation.resume(Result.ShouldRequestPermissionRationale)
                    }
                }
                else -> {
                    Log.d(tag, "askPermission - $permission: not granted, requesting permission")
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }
}