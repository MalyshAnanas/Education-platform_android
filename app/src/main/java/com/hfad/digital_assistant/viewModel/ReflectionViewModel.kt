package com.hfad.digital_assistant.viewModel

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.AnswerRequest
import com.hfad.digital_assistant.model.api.Question
import com.hfad.digital_assistant.model.api.ReflectionApi
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.launch

class ReflectionViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val api = ReflectionApi.ReflectionApiFactory.create(userPreferences)

    private val answers = mutableMapOf<Int, Any>()

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    fun loadQuestions() {
        Log.d("VM", "loadQuestions called")
        viewModelScope.launch {
            try {
                _questions.value = api.getQuestions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setAnswer(questionId: Int, value: Any) {
        answers[questionId] = value
    }

    fun sendAnswers() {
        viewModelScope.launch {
            try {

                val request = answers.map {
                    AnswerRequest(
                        question = it.key,
                        value_int = it.value as? Int,
                        value_text = it.value as? String
                    )
                }

                api.sendAnswers(request)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAnswer(questionId: Int): Any? {
        return answers[questionId]
    }
}