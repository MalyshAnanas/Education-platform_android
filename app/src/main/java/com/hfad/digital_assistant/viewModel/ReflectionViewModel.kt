package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.AnswerRequest
import com.hfad.digital_assistant.model.api.Question
import com.hfad.digital_assistant.model.api.ReflectionApi
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.launch
import java.util.Calendar

class ReflectionViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val api = ReflectionApi.ReflectionApiFactory.create(userPreferences)

    private val answers = mutableMapOf<Int, Any>()

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFormChanged = MutableLiveData(false)
    val isFormChanged: LiveData<Boolean> = _isFormChanged

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate

    private val _isToday = MutableLiveData(true)
    val isToday: LiveData<Boolean> = _isToday

    fun isReadOnly(): Boolean {
        return _isToday.value != true
    }

    fun loadQuestions() {
        viewModelScope.launch {
            val result = api.getQuestions()

            _questions.value = result
            answers.clear()

            result.forEach { question ->
                question.user_answer?.let { answer ->
                    when (question.type) {
                        "choice" -> answer.value_int?.let {
                            answers[question.id] = it
                        }
                        "text" -> answer.value_text?.let {
                            answers[question.id] = it
                        }
                    }
                }
            }

            _isFormChanged.value = false
            _isToday.value = true
        }
    }

    fun getAnswer(id: Int) = answers[id]

    fun setAnswer(id: Int, value: Any) {
        answers[id] = value
        _isFormChanged.value = true
    }

    fun sendAnswers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = answers.map {
                    AnswerRequest(
                        question = it.key,
                        value_int = it.value as? Int,
                        value_text = it.value as? String
                    )
                }

                api.sendAnswers(request)
                loadQuestions()

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadQuestionsForDate(date: String) {
        viewModelScope.launch {
            _selectedDate.value = date

            val today = getTodayDate()
            _isToday.value = (date == today)

            val result = api.getQuestions()
            _questions.value = result
        }
    }

    fun loadToday() {
        loadQuestions()
    }

    private fun getTodayDate(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)+1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}