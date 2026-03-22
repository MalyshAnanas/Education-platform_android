package com.hfad.digital_assistant.model.api

data class GoalRequest(
    val goal: String
)

data class GoalResponse(
    val goal: String?
)

data class QuoteResponse(
    val text: String
)