package com.hfad.digital_assistant.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.local.LibraryDao
import com.hfad.digital_assistant.model.local.LocalLibraryRepository
import kotlinx.coroutines.launch

class RouteViewModel(
    private val remoteRepository: RemoteLibraryRepository,
    private val libraryDao: LibraryDao
) : ViewModel() {


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
                localRepository.replaceAll(remoteFiles) // сохраняем локально
                _libraryFiles.value = remoteFiles
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Получение одного файла
    fun getFile(slug: String): LiveData<LibraryFile?> {
        val result = MutableLiveData<LibraryFile?>()
        viewModelScope.launch {
            // Сначала пробуем взять из локальной базы
            val localFile = localRepository.getFile(slug)
            if (localFile != null) {
                result.value = localFile
            } else {
                // Если нет — загружаем с API и сохраняем
                try {
                    val remoteFile = remoteRepository.getFile(slug)
                    localRepository.saveFile(remoteFile)
                    result.value = remoteFile
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
        return result
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
}