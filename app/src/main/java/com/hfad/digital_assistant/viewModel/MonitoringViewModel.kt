package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.*
import com.hfad.digital_assistant.model.api.MonitoringRepository
import com.hfad.digital_assistant.model.api.CurrentIndicatorDto
import com.hfad.digital_assistant.model.api.HistoryPeriodDto
import com.hfad.digital_assistant.model.api.IndicatorUpdateItemDto
import kotlinx.coroutines.launch

class MonitoringViewModel(
    private val repository: MonitoringRepository
) : ViewModel() {

    val indicators = MutableLiveData<List<CurrentIndicatorDto>>()
    val history = MutableLiveData<List<HistoryPeriodDto>>()
    val isLoading = MutableLiveData(false)
    val message = MutableLiveData<String>()

    fun loadIndicators() {
        viewModelScope.launch {
            try {
                isLoading.value = true

                val response = repository.getCurrentIndicators()

                if (response.isSuccessful) {
                    val body = response.body().orEmpty()

                    indicators.value = body

                    if (body.isEmpty()) {
                        message.value = "Сервер вернул пустой список вопросов"
                    } else {
                        message.value = "Загружено вопросов: ${body.size}"
                    }

                } else {
                    message.value = "Ошибка сервера: ${response.code()}"
                }

            } catch (e: Exception) {
                message.value = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun sendAnswers(answers: List<IndicatorUpdateItemDto>) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val response = repository.sendAnswers(answers)

                if (response.isSuccessful) {
                    message.value = "Ответы отправлены"
                    loadHistory()
                } else {
                    message.value = "Ошибка отправки ответов"
                }
            } catch (e: Exception) {
                message.value = "Не удалось отправить ответы"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                history.value = repository.getHistory()
            } catch (e: Exception) {
                message.value = "Не удалось загрузить историю"
            }
        }
    }
}