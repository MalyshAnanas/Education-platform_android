package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegistrationViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return RegistrationViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}