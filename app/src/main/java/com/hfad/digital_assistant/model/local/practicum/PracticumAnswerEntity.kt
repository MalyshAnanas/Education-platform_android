package com.hfad.digital_assistant.model.local.practicum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practicum_answers")
data class PracticumAnswerEntity(
    @PrimaryKey val id: Int,

    val caseId: Int,

    val text: String,
    val status: String,
    val comment: String?
)