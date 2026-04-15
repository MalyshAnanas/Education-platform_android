package com.hfad.digital_assistant.model.api

data class Question(
    val id: Int,
    val text: String,
    val type: String,
    val user_answer: Answer?
)

data class Answer(
    val id: Int?,
    val question_id: Int?,
    val value_int: Int?,
    val value_text: String?,
    val created_at: String?
)

data class AnswerRequest(
    val question: Int,
    val value_int: Int?,
    val value_text: String?
)

data class AnswerBulkRequest(
    val answers: List<AnswerRequest>
)

data class QuestionHistory(
    val id: Int,
    val text: String,
    val type: String,
    val answers: List<AnswerHistoryItem>
)

data class AnswerHistoryItem(
    val value_int: Int?,
    val value_text: String?,
    val created_at: String?
)