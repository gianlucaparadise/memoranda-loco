package com.gianlucaparadise.memorandaloco.preference

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.gianlucaparadise.memorandaloco.vo.NotificationRecord
import com.gianlucaparadise.memorandaloco.vo.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This lets you use the shared preference in the viewmodel
 */
@Singleton
class PreferenceHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("private_preferences", MODE_PRIVATE)

    private var _lastHomeNotification: NotificationRecord? = null
    var lastHomeNotification: NotificationRecord?
        get() {
            if (_lastHomeNotification == null) {
                val time = preferences.getLong("lastHomeNotification_time", 0)

                val triggerTypeRaw = preferences.getInt("lastHomeNotification_triggerType", 0)
                val triggerType =
                    NotificationRecord.TriggerType.values()
                        .first { it.value.toInt() == triggerTypeRaw }

                _lastHomeNotification = NotificationRecord(triggerType, time)
            }
            return _lastHomeNotification
        }
        set(value) {
            // Apply method is asynchronous, but the backing field prevents race conditions
            if (value == null) {
                preferences.edit()
                    .remove("lastHomeNotification_time")
                    .remove("lastHomeNotification_triggerType")
                    .apply()
            } else {
                preferences.edit()
                    .putLong("lastHomeNotification_time", value.time)
                    .putInt("lastHomeNotification_triggerType", value.triggeredOn.value.toInt())
                    .apply()
            }
            _lastHomeNotification = value
        }

    private var _appToOpen: String? = null
    var appToOpen: String?
        get() {
            if (_appToOpen == null) {
                _appToOpen = preferences.getString("appToOpen_packageName", null)
            }
            return _appToOpen
        }
        set(value) {
            // Apply method is asynchronous, but the backing field prevents some race conditions
            if (value == null) {
                preferences.edit()
                    .remove("appToOpen_packageName")
                    .apply()
            } else {
                preferences.edit()
                    .putString("appToOpen_packageName", value)
                    .apply()
            }
            _appToOpen = value
        }
}