package com.hfad.digital_assistant.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.local.LibraryDao
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.local.LocalLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MainViewModel(
    private val remoteRepository: RemoteLibraryRepository,
    private val libraryDao: LibraryDao
): ViewModel() {
    private val localRepository = LocalLibraryRepository(libraryDao)
    private val _libraryFiles = MutableLiveData<List<LibraryFile>>()
    val libraryFiles: LiveData<List<LibraryFile>> = _libraryFiles

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Загружаем файлы из локальной базы
    fun loadLocalFiles() {
        viewModelScope.launch {
            _libraryFiles.value = localRepository.getAllFiles()
        }
    }

    // Обновляем файлы с API и сохраняем локально
    fun refreshFiles() {
        Log.i("RouteViewModel", "до viewModelScope")
        viewModelScope.launch {
            try {
                Log.i("RouteViewModel", "до remoteFiles")
                val remoteFiles = remoteRepository.getAllFiles() // вызываем API
                Log.i("RouteViewModel", remoteFiles.toString())
                _libraryFiles.value = remoteFiles
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Поиск по локальной базе
    fun searchFiles(query: String) {
        viewModelScope.launch {
            _libraryFiles.value = localRepository.searchFiles(query)
        }
    }

    // Фильтрация по типу
    fun filterByType(type: String) {
        viewModelScope.launch {
            _libraryFiles.value = localRepository.getFilesByType(type)
        }
    }

    // Загрузка файла на сервер
    fun uploadFile(uri: Uri, fileName: String, context: Context) {
        viewModelScope.launch {
            try {
                val fileName = getFileName(uri, context)
                val inputStream = context.contentResolver.openInputStream(uri)!!
                val bytes = inputStream.readBytes()
                inputStream.close()

                val fileExtension = fileName.substringAfterLast('.', "").lowercase().let { ".$it" }
                val mimeTypes = mapOf(
                    ".pdf" to "application/pdf",
                    ".doc" to "application/msword",
                    ".docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    ".odt" to "application/vnd.oasis.opendocument.text",
                    ".ppt" to "application/vnd.ms-powerpoint",
                    ".pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    ".jpg" to "image/jpeg",
                    ".jpeg" to "image/jpeg",
                    ".png" to "image/png",
                    ".mp4" to "video/mp4"
                )
                val mimeType = mimeTypes[fileExtension] ?: "application/octet-stream"

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

                val titleBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = "Загружен пользователем".toRequestBody("text/plain".toMediaTypeOrNull())

                val uploadedFile = remoteRepository.uploadFile(titleBody,
                    descriptionBody, filePart)

                val localFile = LibraryFile(
                    slug = uploadedFile.slug,
                    title = uploadedFile.title,
                    description = uploadedFile.description,
                    file_type = uploadedFile.file_type,
                    file = uploadedFile.file,
                    author_name = "Пользователь",
                    created_at = System.currentTimeMillis().toString(),
                    category_details = emptyList()
                )
                libraryDao.insertFile(localFile)
                loadLocalFiles()
            } catch (e: Exception) {
                Log.e("UPLOAD_DEBUG", "Upload error", e)
                throw e
            }
        }
    }

    // Вспомогательная функция для имени файла
    private fun getFileName(uri: Uri, context: Context): String {
        var name = "file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(index)
            }
        }
        return name
    }

    // Указываем путь для скачивания документов (/data/data/digital_assistant/files/)
    fun downloadFile(
        file: LibraryFile,
        context: Context,
        onFinished: (LibraryFile) -> Unit
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(file.file)
                    .addHeader("Accept", "*/*")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("DOWNLOAD", "Ошибка ${response.code}")
                    return@launch
                }

                val body = response.body ?: return@launch

                val extension = file.file.substringAfterLast(".", "dat")

                val localFile = File(
                    context.filesDir,
                    "${file.slug}.$extension"
                )

                body.byteStream().use { input ->
                    FileOutputStream(localFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val updatedFile = file.copy(
                    localPath = localFile.absolutePath
                )

                libraryDao.insertFile(updatedFile)

                withContext(Dispatchers.Main) {
                    onFinished(updatedFile)
                }

            } catch (e: Exception) {
                Log.e("DOWNLOAD", "Ошибка скачивания", e)
            }
        }
    }
}