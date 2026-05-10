package com.hfad.digital_assistant.model.api

data class GoalRequest(
    val text: String
)

data class GoalResponse(
    val id: Int?,
    val text: String?,
    val created_at: String?
)

data class QuoteResponse(
    val quote: String?
)