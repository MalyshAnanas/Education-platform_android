package com.hfad.digital_assistant.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.local.LibraryDao

class RouteViewModelFactory(
    private val remoteRepository: RemoteLibraryRepository,
    private val libraryDao: LibraryDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("RouteViewModelFactory", "до if")

        if (modelClass.isAssignableFrom(RouteViewModel::class.java)) {
            Log.d("RouteViewModelFactory", "после if")
            @Suppress("UNCHECKED_CAST")
            return RouteViewModel(remoteRepository, libraryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}