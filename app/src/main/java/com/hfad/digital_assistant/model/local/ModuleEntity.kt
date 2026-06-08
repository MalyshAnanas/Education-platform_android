package com.hfad.digital_assistant.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val type: String,
    val order: Int
)