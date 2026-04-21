package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.AuthRepository
import com.hfad.digital_assistant.model.api.ProfileUiState
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _logoutEvent = MutableLiveData<Unit>()
    val logoutEvent: LiveData<Unit> = _logoutEvent

    fun loadProfile() {
        val localPhotoUri = userPreferences.getPhotoUri()

        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            photoUri = localPhotoUri
        )

        viewModelScope.launch {
            val result = authRepository.getProfile()

            result.onSuccess { user ->
                _uiState.value = ProfileUiState(
                    fullName = user.profile.full_name ?: user.username,
                    position = user.profile.position.orEmpty(),
                    organization = user.profile.organization.orEmpty(),
                    photoUri = localPhotoUri,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value?.copy(isLoading = false)
                _message.value = it.message ?: "Не удалось загрузить профиль"
            }
        }
    }

    fun saveProfile(
        fullName: String,
        position: String,
        organization: String
    ) {
        if (fullName.isBlank()) {
            _message.value = "Введите ФИО"
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.updateProfile(
                fullName = fullName,
                position = position,
                organization = organization
            )

            result.onSuccess {
                _uiState.value = _uiState.value?.copy(
                    fullName = fullName,
                    position = position,
                    organization = organization,
                    isLoading = false
                )
                _message.value = "Профиль обновлён"
            }.onFailure {
                _uiState.value = _uiState.value?.copy(isLoading = false)
                _message.value = it.message ?: "Не удалось обновить профиль"
            }
        }
    }

    fun savePhotoLocally(uri: String) {
        userPreferences.savePhotoUri(uri)
        _uiState.value = _uiState.value?.copy(photoUri = uri)
        _message.value = "Фото сохранено"
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutEvent.value = Unit
        }
    }
}