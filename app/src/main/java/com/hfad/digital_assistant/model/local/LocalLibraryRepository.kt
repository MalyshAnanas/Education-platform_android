package com.hfad.digital_assistant.model.local

class LocalLibraryRepository(
    private val libraryDao: LibraryDao
) {

    // Получить все файлы
    suspend fun getAllFiles(): List<LibraryFile> {
        return libraryDao.getAllFiles()
    }

    // Получить файл по slug
    suspend fun getFile(slug: String): LibraryFile? {
        return libraryDao.getFile(slug)
    }

    // Сохранить один файл
    suspend fun saveFile(file: LibraryFile) {
        libraryDao.insertFile(file)
    }

    // Сохранить список файлов
    suspend fun saveFiles(files: List<LibraryFile>) {
        libraryDao.insertFiles(files)
    }

    // Обновить файл
    suspend fun updateFile(file: LibraryFile) {
        libraryDao.updateFile(file)
    }

    // Удалить файл
    suspend fun deleteFile(slug: String) {
        libraryDao.deleteFile(slug)
    }

    // Поиск
    suspend fun searchFiles(query: String): List<LibraryFile> {
        return libraryDao.searchFiles(query)
    }

    // Получить по типу
    suspend fun getFilesByType(type: String): List<LibraryFile> {
        return libraryDao.getFilesByType(type)
    }

    // Очистить и сохранить новые (для синхронизации с сервером)
    suspend fun replaceAll(files: List<LibraryFile>) {
        libraryDao.insertFiles(files)
    }
}