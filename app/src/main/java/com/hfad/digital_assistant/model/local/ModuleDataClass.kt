package com.hfad.digital_assistant.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_modules")
data class ModuleCompletionEntity(
    @PrimaryKey val moduleId: Int
)