package com.gianlucaparadise.memorandaloco.bluetooth

import android.bluetooth.BluetoothAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothHelper @Inject constructor() {
    /**
     * Gets the BluetoothAdapter
     * This may be null when bluetooth is not a feature on the phone (i.e. Emulator)
     */
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    var isBluetoothEnabled: Boolean
        get() = adapter?.isEnabled ?: false
        set(shouldEnable) {
            if (adapter == null) return

            if (shouldEnable && !adapter.isEnabled) {
                adapter.enable()
            } else if (!shouldEnable && adapter.isEnabled) {
                adapter.disable()
            }
        }
}