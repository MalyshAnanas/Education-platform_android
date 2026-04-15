package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.AnswerBulkRequest
import com.hfad.digital_assistant.model.api.AnswerRequest
import com.hfad.digital_assistant.model.api.Question
import com.hfad.digital_assistant.model.api.QuestionHistory
import com.hfad.digital_assistant.model.api.ReflectionApi
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.launch
import java.util.Calendar

class ReflectionViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val api = ReflectionApi.ReflectionApiFactory.create(userPreferences)

    private val answers = mutableMapOf<Int, Any>()
    private val originalAnswers = mutableMapOf<Int, Any>()

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFormChanged = MutableLiveData(false)
    val isFormChanged: LiveData<Boolean> = _isFormChanged

    private val _selectedDate = MutableLiveData(getTodayDate())
    val selectedDate: LiveData<String> = _selectedDate

    private val _isToday = MutableLiveData(true)
    val isToday: LiveData<Boolean> = _isToday

    private val _isEditable = MutableLiveData(true)
    val isEditable: LiveData<Boolean> = _isEditable

    private val _hasAnswersForCurrentDay = MutableLiveData(false)
    val hasAnswersForCurrentDay: LiveData<Boolean> = _hasAnswersForCurrentDay

    private val _buttonText = MutableLiveData("Отправить")
    val buttonText: LiveData<String> = _buttonText

    fun getAnswer(id: Int): Any? = answers[id]

    fun setAnswer(id: Int, value: Any) {
        answers[id] = value
        _isFormChanged.value = answers != originalAnswers
    }

    fun loadQuestions() {
        loadQuestionsForDate(getTodayDate())
    }

    fun loadToday() {
        loadQuestionsForDate(getTodayDate())
    }

    fun loadQuestionsForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedDate.value = date

                val today = getTodayDate()
                val isTodayDate = date == today
                _isToday.value = isTodayDate

                val activeQuestions = api.getQuestions()
                _questions.value = activeQuestions

                if (isTodayDate) {
                    fillFromTodayAnswers(activeQuestions)
                } else {
                    val history = api.getAnswersHistory()
                    fillFromHistory(activeQuestions, history, date)
                }

                val hasAnswers = answers.isNotEmpty()
                _hasAnswersForCurrentDay.value = hasAnswers

                when {
                    !isTodayDate -> {
                        _isEditable.value = false
                        _buttonText.value = "Просмотр"
                    }

                    hasAnswers -> {
                        _isEditable.value = false
                        _buttonText.value = "Изменить"
                    }

                    else -> {
                        _isEditable.value = true
                        _buttonText.value = "Отправить"
                    }
                }

                _isFormChanged.value = false

            } catch (e: Exception) {
                _questions.value = emptyList()
                answers.clear()
                originalAnswers.clear()
                _hasAnswersForCurrentDay.value = false
                _isEditable.value = false
                _buttonText.value = "Просмотр"
                _isFormChanged.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onMainButtonClicked() {
        val isToday = _isToday.value == true
        val isEditableNow = _isEditable.value == true
        val hasAnswersNow = _hasAnswersForCurrentDay.value == true

        if (!isToday) return

        if (!isEditableNow && hasAnswersNow) {
            _isEditable.value = true
            _buttonText.value = "Сохранить"
            _isFormChanged.value = false
            return
        }

        if (isEditableNow) {
            sendAnswers()
        }
    }

    private fun sendAnswers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AnswerBulkRequest(
                    answers = answers.map { entry ->
                        AnswerRequest(
                            question = entry.key,
                            value_int = entry.value as? Int,
                            value_text = entry.value as? String
                        )
                    }
                )

                val response = api.sendAnswers(request)

                if (response.isSuccessful) {
                    loadQuestionsForDate(getTodayDate())
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fillFromTodayAnswers(result: List<Question>) {
        answers.clear()
        originalAnswers.clear()

        result.forEach { question ->
            question.user_answer?.let { answer ->
                when (question.type) {
                    "choice" -> {
                        answer.value_int?.let {
                            answers[question.id] = it
                            originalAnswers[question.id] = it
                        }
                    }

                    "text" -> {
                        answer.value_text?.let {
                            answers[question.id] = it
                            originalAnswers[question.id] = it
                        }
                    }
                }
            }
        }
    }

    private fun fillFromHistory(
        activeQuestions: List<Question>,
        history: List<QuestionHistory>,
        date: String
    ) {
        answers.clear()
        originalAnswers.clear()

        val historyMap = history.associateBy { it.id }

        activeQuestions.forEach { question ->
            val historyItem = historyMap[question.id] ?: return@forEach
            val answerForDate = historyItem.answers.firstOrNull {
                extractDate(it.created_at) == date
            }

            answerForDate?.let { answer ->
                when (question.type) {
                    "choice" -> {
                        answer.value_int?.let {
                            answers[question.id] = it
                            originalAnswers[question.id] = it
                        }
                    }

                    "text" -> {
                        answer.value_text?.let {
                            answers[question.id] = it
                            originalAnswers[question.id] = it
                        }
                    }
                }
            }
        }
    }

    private fun extractDate(dateTime: String?): String? {
        return dateTime?.substringBefore("T")
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$year-$month-$day"
    }
}