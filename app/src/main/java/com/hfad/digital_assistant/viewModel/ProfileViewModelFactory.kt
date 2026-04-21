package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.AuthRepository
import com.hfad.digital_assistant.model.api.UserPreferences

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(authRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}