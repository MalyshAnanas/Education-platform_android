package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.LibraryFile

data class Module(
    val id: Int,
    val title: String,
    val type: String, // theory / practice / reflection
    val order: Int,
    val items: List<ModuleItem>
)

data class ModuleItem(
    val id: Int,
    val type: String,
    val text: String?,
    val library_file: LibraryFile?,
    val order: Int
)