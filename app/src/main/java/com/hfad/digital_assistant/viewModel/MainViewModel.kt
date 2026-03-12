package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.local.LibraryDao
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.local.LocalLibraryRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val libraryDao: LibraryDao
): ViewModel() {
    private val localRepository = LocalLibraryRepository(libraryDao)
    private val _libraryFiles = MutableLiveData<List<LibraryFile>>()
    val libraryFiles: LiveData<List<LibraryFile>> = _libraryFiles

    // Загружаем файлы из локальной базы
    fun loadLocalFiles() {
        viewModelScope.launch {
            _libraryFiles.value = localRepository.getAllFiles()
        }
    }
}