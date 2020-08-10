package com.gianlucaparadise.memorandaloco.vo

import androidx.room.*

@Entity
data class Reminder(
    @PrimaryKey val uid: Int,
    val name: String,
    @Embedded(prefix = "location_") val location: LocationDescriptor,
    val triggerWhen: TriggerType,
    @Embedded(prefix = "notificationAction_") val notificationAction: NotificationAction
) {
    enum class TriggerType(val value: Byte) {
        None(0),
        Leaving(1),
        Dwelling(2)
    }
}