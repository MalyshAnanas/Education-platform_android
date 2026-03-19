package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadModules() {
        viewModelScope.launch {
            try {
                _modules.value = repository.getModules()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markCompleted(moduleId: Int) {
        val updated = _completedModules.value?.toMutableSet() ?: mutableSetOf()
        updated.add(moduleId)
        _completedModules.value = updated
    }
}