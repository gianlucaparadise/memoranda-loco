package com.gianlucaparadise.memorandaloco.vo

data class NotificationAction(
    val actionType: Type,
    val parameter: String = ""
) {
    enum class Type(val value: Byte) {
        NoAction(0),

        /**
         * When the notification is tapped, the app specified at `parameter` is opened
         */
        OpenAnotherApp(1),
        /**
         * When the notification is tapped, the bluetooth is turned off
         */
        TurnOffBluetooth(2)
    }
}