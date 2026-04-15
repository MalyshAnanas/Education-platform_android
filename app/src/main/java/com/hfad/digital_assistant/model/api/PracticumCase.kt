package com.hfad.digital_assistant.model.api

data class PracticumCase(
    val id: Int,
    val name: String,
    val description: String,
    val answers: List<PracAnswer>
)

data class PracAnswer(
    val id: Int,
    val text: String,
    val status: String,
    val comment: String?
)

data class PracAnswerRequest(
    val case: Int,
    val text: String
)

data class PracAnswerResponse(
    val id: Int,
    val status: String
)

enum class CaseStatus {
    OPEN,
    CHECKING,
    DONE
}