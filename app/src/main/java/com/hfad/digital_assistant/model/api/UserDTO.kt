package com.hfad.digital_assistant.model.api

data class UserDto(
    val id: Int,
    val username: String,
    val email: String?,
    val profile: ProfileDto
)

data class ProfileDto(
    val full_name: String?,
    val position: String?,
    val organization: String?
)
