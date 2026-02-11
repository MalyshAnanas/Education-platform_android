package com.hfad.digital_assistant.model.api

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val full_name: String
)
