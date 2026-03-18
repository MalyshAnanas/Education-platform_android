package com.hfad.digital_assistant.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.local.LibraryDao

class MainViewModelFactory(
    private val remoteRepository: RemoteLibraryRepository,
    private val libraryDao: LibraryDao
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(remoteRepository, libraryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}