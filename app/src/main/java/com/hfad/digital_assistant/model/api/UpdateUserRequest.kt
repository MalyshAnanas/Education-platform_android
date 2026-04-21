package com.hfad.digital_assistant.model.api

data class UpdateUserRequest(
    val profile: UpdateProfileRequest
)

data class UpdateProfileRequest(
    val full_name: String? = null,
    val position: String? = null,
    val organization: String? = null,
    val photo: String? = null
)
