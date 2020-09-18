package com.gianlucaparadise.memoloco.db

import androidx.room.TypeConverter
import com.gianlucaparadise.memoloco.vo.NotificationAction
import com.gianlucaparadise.memoloco.vo.Place
import com.gianlucaparadise.memoloco.vo.Reminder

class Converters {

    @TypeConverter
    fun triggerTypeFromNum(value: Byte) = Reminder.TriggerType.values().first { it.value == value }

    @TypeConverter
    fun triggerTypeToNum(type: Reminder.TriggerType) = type.value

    @TypeConverter
    fun notificationActionTypeFromNum(value: Byte) =
        NotificationAction.Type.values().first { it.value == value }

    @TypeConverter
    fun notificationActionTypeToNum(type: NotificationAction.Type) = type.value

    @TypeConverter
    fun placeTypeFromNum(value: Byte) = Place.Type.values().first { it.value == value }

    @TypeConverter
    fun placeTypeToNum(type: Place.Type) = type.value
}