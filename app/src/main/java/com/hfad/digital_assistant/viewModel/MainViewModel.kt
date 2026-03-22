package com.hfad.digital_assistant.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.RemoteCurrentRepository
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
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteCurrentRepository: RemoteCurrentRepository,
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
                val remoteFiles = remoteLibraryRepository.getAllFiles() // вызываем API
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

                val uploadedFile = remoteLibraryRepository.uploadFile(titleBody,
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

    // Синхронизация с сервером для целей
    fun syncGoalToServer(goal: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                remoteCurrentRepository.setCurrentGoal(goal)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadGoalFromServer(
        onGoalLoaded: (String?) -> Unit,
        onQuoteLoaded: (String?) -> Unit
    ) {
        Log.d("GOAL_DEBUG", "loadGoalFromServer called")

        viewModelScope.launch {
            try {
                val response = remoteCurrentRepository.getCurrentGoal()

                Log.d("GOAL_DEBUG", "Response code: ${response.code()}")
                Log.d("GOAL_DEBUG", "Response body: ${response.body()}")

                if (response.isSuccessful) {

                    // 200 - OK
                    val goal = response.body()?.goal

                    if (!goal.isNullOrBlank()) {
                        // есть цель
                        onGoalLoaded(goal)
                    } else {
                        // цель пустая => показываем цитату
                        Log.d("GOAL_DEBUG", "Goal empty → loading quote")

                        val quoteResponse = remoteCurrentRepository.getRandomQuote()

                        if (quoteResponse.isSuccessful) {
                            onQuoteLoaded(quoteResponse.body()?.text)
                        }
                    }

                } else {
                    // 404 или любой другой неуспешный код

                    Log.d("GOAL_DEBUG", "Request failed → fallback to quote")

                    val quoteResponse = remoteCurrentRepository.getRandomQuote()

                    if (quoteResponse.isSuccessful) {
                        onQuoteLoaded(quoteResponse.body()?.text)
                    }
                }

            } catch (e: Exception) {
                Log.e("GOAL_DEBUG", "Exception", e)
                _error.value = e.message

                //  fallback даже при ошибке сети
                try {
                    val quoteResponse = remoteCurrentRepository.getRandomQuote()

                    if (quoteResponse.isSuccessful) {
                        onQuoteLoaded(quoteResponse.body()?.text)
                    }
                } catch (ex: Exception) {
                    Log.e("GOAL_DEBUG", "Quote fallback failed", ex)
                }
            }
        }
    }

    fun deleteGoalFromServer(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                remoteCurrentRepository.deleteCurrentGoal()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}