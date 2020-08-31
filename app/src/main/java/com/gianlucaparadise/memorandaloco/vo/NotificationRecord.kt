package com.gianlucaparadise.memorandaloco.vo

data class NotificationRecord(val triggeredOn: TriggerType, val time: Long) {

    enum class TriggerType(val value: Byte) {
        None(0),
        Leaving(1),
        Dwelling(2)
    }
}