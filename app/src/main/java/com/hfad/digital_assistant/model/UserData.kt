package com.hfad.digital_assistant.model

/**
 * Класс данных пользователя, полученных после авторизации.
 */
data class UserData(
    val fullName: String,
    val email: String? = null
)