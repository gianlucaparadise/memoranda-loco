package com.gianlucaparadise.memorandaloco

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gianlucaparadise.memorandaloco.bluetooth.BluetoothHelper
import com.gianlucaparadise.memorandaloco.notification.NotificationHelper
import com.gianlucaparadise.memorandaloco.vo.NotificationAction
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    @Inject
    lateinit var bluetoothHelper: BluetoothHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val notificationActionExtra =
            intent.getSerializableExtra(NotificationHelper.NOTIFICATION_ACTION_EXTRA) as? NotificationAction
        parseNotificationAction(notificationActionExtra)
    }

    private fun parseNotificationAction(notificationAction: NotificationAction?) {
        if (notificationAction == null) return

        when (notificationAction.actionType) {
            NotificationAction.Type.TurnOffBluetooth -> {
                bluetoothHelper.isBluetoothEnabled = false
                displayBluetoothDisabledMessage()
            }
            else -> {
                Log.d(tag, "Skipped NotificationAction Type: ${notificationAction.actionType}")
            }
        }
    }

    private fun displayBluetoothDisabledMessage() {
        val mySnackbar = Snackbar.make(
            findViewById(android.R.id.content),
            R.string.bluetooth_disabled_popup,
            Snackbar.LENGTH_INDEFINITE
        )

        mySnackbar.setAction(R.string.btn_ok) { }
        mySnackbar.setActionTextColor(getColor(R.color.colorPrimary))
        mySnackbar.show()
    }
}