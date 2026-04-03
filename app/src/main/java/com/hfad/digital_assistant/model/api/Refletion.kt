package com.hfad.digital_assistant.model.api

data class Question(
    val id: Int,
    val text: String,
    val type: String, // "choice" или "text"
    var answer: String = ""
)

data class Answer(
    val id: Int?,
    val value_int: Int?,
    val value_text: String?,
    val created_at: String?
)

data class AnswerRequest(
    val question: Int,
    val value_int: Int?,
    val value_text: String?
)