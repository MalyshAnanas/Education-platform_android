package com.hfad.digital_assistant.model.api

data class ProfileUiState(
    val fullName: String = "",
    val position: String = "",
    val organization: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false
)