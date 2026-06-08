package com.hfad.digital_assistant.model.local.practicum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_practicum_answers")
data class PendingPracticumAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val caseId: Int,
    val text: String
)