package com.hfad.digital_assistant.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.RemoteCurrentRepository
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.local.LibraryDao

class MainViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteCurrentRepository: RemoteCurrentRepository,
    private val libraryDao: LibraryDao
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(remoteLibraryRepository, remoteCurrentRepository, libraryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}