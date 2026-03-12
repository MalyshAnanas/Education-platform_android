package com.hfad.digital_assistant.model.api

data class UploadFileRequest(
    val title: String,
    val description: String,
    val file: String
)