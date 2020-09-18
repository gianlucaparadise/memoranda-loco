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

    fun hasBackgroundLocationPermission() =
        hasPermission(backgroundLocationPermission)

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