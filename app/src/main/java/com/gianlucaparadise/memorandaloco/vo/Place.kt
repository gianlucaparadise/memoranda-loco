package com.gianlucaparadise.memorandaloco.vo

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Place(
    @PrimaryKey(autoGenerate = true) val uid: Int = Int.MIN_VALUE,
    val name: String,
    val type: Type,
    @Embedded(prefix = "location_") val location: LocationDescriptor
) {
    enum class Type(val value: Byte) {
        Generic(0),
        Home(1)
    }
}