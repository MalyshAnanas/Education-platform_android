package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.UserPreferences

class ReflectionViewModelFactory(
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReflectionViewModel(userPreferences) as T
    }
}