package com.gianlucaparadise.memoloco.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsChecker @Inject constructor(@ApplicationContext private val context: Context) {

    fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(it) }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starting from Android 11 (API 30), background location permissions can be requested only after
     * the foreground location permissions. The request will navigate the user to the App's settings page
     * where the permissions can be accepted
     */
    val canRequestBackgroundPermissions: Boolean
        get() {
            return if (needsTwoStepsPermissionRequest)
                hasForegroundLocationPermission()
            else
                true
        }

    /**
     * I need a two steps permission request when I'm on API Level 30 and higher:
     * - First step: request foreground location permissions
     * - Second step: navigate the user to the App's settings and ask to choose "Allow all the time"
     */
    val needsTwoStepsPermissionRequest: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun hasBackgroundLocationPermission() =
        hasPermission(backgroundLocationPermission)

    fun hasForegroundLocationPermission() =
        hasPermission(foregroundLocationPermission)

    /**
     * This is used to check that the user has given foreground location permission
     */
    private val foregroundLocationPermission: String = Manifest.permission.ACCESS_FINE_LOCATION

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