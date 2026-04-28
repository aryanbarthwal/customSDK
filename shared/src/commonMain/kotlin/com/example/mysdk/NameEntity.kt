package com.example.mysdk

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: String,
)
