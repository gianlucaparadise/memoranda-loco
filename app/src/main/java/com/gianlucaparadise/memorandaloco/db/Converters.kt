package com.gianlucaparadise.memorandaloco.db

import androidx.room.TypeConverter
import com.gianlucaparadise.memorandaloco.vo.NotificationAction
import com.gianlucaparadise.memorandaloco.vo.Reminder

class Converters {

    @TypeConverter
    fun triggerTypeFromNum(value: Byte) = Reminder.TriggerType.values().first { it.value == value }

    @TypeConverter
    fun triggerTypeToNum(type: Reminder.TriggerType) = type.value

    @TypeConverter
    fun notificationActionTypeFromNum(value: Byte) = NotificationAction.Type.values().first { it.value == value }

    @TypeConverter
    fun notificationActionToNum(type: NotificationAction.Type) = type.value
}