package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.RouteRepository

class RouteViewModelFactory(
    private val repository: RouteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RouteViewModel(repository) as T
    }
}