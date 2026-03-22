package com.hfad.digital_assistant.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.hfad.digital_assistant.model.api.Module
import com.hfad.digital_assistant.model.api.RouteRepository
import kotlinx.coroutines.launch

class RouteViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _modules = MutableLiveData<List<Module>>()
    val modules: LiveData<List<Module>> = _modules

    private val _completedModules = MutableLiveData<Set<Int>>(emptySet())
    val completedModules: LiveData<Set<Int>> = _completedModules

    // Загружаем модули
    fun loadModules() {
        viewModelScope.launch {
            try {
                // 1. Загружаем модули
                _modules.value = repository.getModules()

                // 2. Загружаем локальные completed
                _completedModules.value = repository.getCompletedModules()

                // 3. Синхронизация с сервером
                repository.syncWithServer()

            } catch (e: Exception) {
                Log.e("RouteViewModel", "Ошибка загрузки", e)
            }
        }
    }

    fun toggleCompleted(moduleId: Int) {
        viewModelScope.launch {
            repository.toggleCompleted(moduleId)

            // обновляем UI из локальной БД
            _completedModules.value = repository.getCompletedModules()
        }
    }

    fun isCompleted(moduleId: Int): Boolean {
        return _completedModules.value?.contains(moduleId) == true
    }
}