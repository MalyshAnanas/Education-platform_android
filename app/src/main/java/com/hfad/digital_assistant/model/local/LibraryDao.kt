package com.hfad.digital_assistant.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hfad.digital_assistant.model.local.LibraryFile

@Dao
interface LibraryDao {

    // Получить все файлы
    @Query("SELECT * FROM library_files ORDER BY created_at DESC")
    suspend fun getAllFiles(): List<LibraryFile>

    // Получить один файл по slug
    @Query("SELECT * FROM library_files WHERE slug = :slug LIMIT 1")
    suspend fun getFile(slug: String): LibraryFile?

    // Добавить или обновить один файл
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: LibraryFile)

    // Добавить или обновить несколько файлов
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<LibraryFile>)

    // Обновить существующий файл
    @Update
    suspend fun updateFile(file: LibraryFile)

    // Удалить файл
    @Query("DELETE FROM library_files WHERE slug = :slug")
    suspend fun deleteFile(slug: String)

    // Поиск по названию или описанию
    @Query("SELECT * FROM library_files WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchFiles(query: String): List<LibraryFile>

    // Получить файлы по типу
    @Query("SELECT * FROM library_files WHERE file_type = :type ORDER BY created_at DESC")
    suspend fun getFilesByType(type: String): List<LibraryFile>
}