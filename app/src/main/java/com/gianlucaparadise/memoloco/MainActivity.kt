package com.gianlucaparadise.memoloco

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.gianlucaparadise.memoloco.alert.AlertHelper
import com.gianlucaparadise.memoloco.bluetooth.BluetoothHelper
import com.gianlucaparadise.memoloco.notification.NotificationHelper
import com.gianlucaparadise.memoloco.vo.NotificationAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    @Inject
    lateinit var bluetoothHelper: BluetoothHelper

    @Inject
    lateinit var alertHelper: AlertHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val notificationActionExtra =
            intent.getSerializableExtra(NotificationHelper.NOTIFICATION_ACTION_EXTRA) as? NotificationAction
        parseNotificationAction(notificationActionExtra)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_privacy_policy -> {
                val url = getString(R.string.privacy_policy_link)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
                true
            }
            else -> {
                false
            }
        }
    }

    private fun parseNotificationAction(notificationAction: NotificationAction?) {
        if (notificationAction == null) return

        when (notificationAction.actionType) {
            NotificationAction.Type.TurnOffBluetooth -> {
                bluetoothHelper.isBluetoothEnabled = false
                alertHelper.showSnackbar(AlertHelper.MessageType.BluetoothDisabled)
            }
            else -> {
                Log.d(tag, "Skipped NotificationAction Type: ${notificationAction.actionType}")
            }
        }
    }
}