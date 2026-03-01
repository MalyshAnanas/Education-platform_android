package com.hfad.digital_assistant.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hfad.digital_assistant.model.local.Category
import com.hfad.digital_assistant.model.local.CategoryConverter

@Entity(tableName = "library_files")
@TypeConverters(CategoryConverter::class)
data class LibraryFile(
    @PrimaryKey val slug: String,
    val title: String,
    val description: String,
    val file_type: String, // document, video, image
    val file: String, // URL или путь к файлу
    val author_name: String,
    val created_at: String, // ISO-строка
    val category_details: List<Category> = emptyList() // Список категорий
)