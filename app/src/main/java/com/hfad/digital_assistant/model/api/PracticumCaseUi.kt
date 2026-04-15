package com.hfad.digital_assistant.model.api

data class PracticumCaseUi(
    val id: Int,
    val name: String,
    val description: String,
    val status: CaseUiStatus,
    val adminComment: String?,
    val userAnswer: String?
)

enum class CaseUiStatus {
    OPEN,
    CHECKING,
    DONE
}