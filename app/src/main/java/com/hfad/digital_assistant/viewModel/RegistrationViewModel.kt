package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.UserData
import com.hfad.digital_assistant.model.api.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registrationResult = MutableStateFlow<Result<UserData>?>(null)
    val registrationResult: StateFlow<Result<UserData>?> = _registrationResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(
        username: String,
        password: String,
        email: String,
        fullName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // Регистрация
            val registerResult = repository.register(
                username = username,
                password = password,
                email = email,
                fullName = fullName
            )

            if (registerResult.isSuccess) {
                // Автологин
                val loginResult = repository.login(username, password)
                _registrationResult.value = loginResult
            } else {
                _registrationResult.value = registerResult
            }

            _isLoading.value = false
        }
    }
}
