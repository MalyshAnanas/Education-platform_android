package com.hfad.digital_assistant.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "module_items")
data class ModuleItemEntity(
    @PrimaryKey val id: Int,

    val moduleId: Int,

    val type: String,
    val text: String?,

    val libraryFileSlug: String?,

    val order: Int
)