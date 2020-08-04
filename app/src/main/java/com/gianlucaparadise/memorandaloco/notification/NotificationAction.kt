package com.gianlucaparadise.memorandaloco.notification

data class NotificationAction(
    val actionType: Type,
    val parameter: String = ""
) {
    enum class Type {
        NoAction,

        /**
         * When the notification is tapped, the app specified at `parameter` is opened
         */
        OpenAnotherApp,
        /**
         * When the notification is tapped, the bluetooth is turned off
         */
        TurnOffBluetooth
    }
}