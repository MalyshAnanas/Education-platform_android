package com.hfad.digital_assistant.model.local.practicum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practicum_cases")
data class PracticumCaseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String
)