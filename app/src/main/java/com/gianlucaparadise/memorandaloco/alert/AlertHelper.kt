package com.gianlucaparadise.memorandaloco.alert

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.gianlucaparadise.memorandaloco.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject

@ActivityScoped
class AlertHelper @Inject constructor(@ActivityContext private val context: Context) {

    fun showSnackbar(type: MessageType) {
        val activity =
            (context as? AppCompatActivity) ?: throw Exception("Context isn't an activity")

        val message = when (type) {
            MessageType.Idle -> "Idle"
            MessageType.BluetoothDisabled -> context.getString(R.string.bluetooth_disabled_popup)
            MessageType.HomeUpdated -> context.getString(R.string.home_updated_popup)
            MessageType.GpsTurnedOffError -> context.getString(R.string.error_gps_turned_off)
        }

        val mySnackbar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_INDEFINITE
        )

        mySnackbar.setAction(R.string.btn_ok) { }
        mySnackbar.setActionTextColor(context.getColor(R.color.colorPrimary))
        mySnackbar.show()
    }

    enum class MessageType {
        Idle,
        BluetoothDisabled,
        HomeUpdated,
        GpsTurnedOffError
    }

}