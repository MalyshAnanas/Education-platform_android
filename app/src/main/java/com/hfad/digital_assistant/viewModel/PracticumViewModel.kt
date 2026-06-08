package com.hfad.digital_assistant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.digital_assistant.model.api.CaseUiStatus
import com.hfad.digital_assistant.model.api.PracticumApi
import com.hfad.digital_assistant.model.api.PracticumCaseUi
import com.hfad.digital_assistant.model.api.PracticumRepository
import com.hfad.digital_assistant.model.api.Question
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.launch

class PracticumViewModel(
    private val repository: PracticumRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _cases = MutableLiveData<List<PracticumCaseUi>>()
    val cases: LiveData<List<PracticumCaseUi>> = _cases
    private val _stats = MutableLiveData<Pair<Int, Int>>()
    val stats: LiveData<Pair<Int, Int>> = _stats

    fun loadCases() {
        viewModelScope.launch {
            try {
                val result = repository.getAllCases()

                val mapped = result.map { case ->

                    val lastAnswer = case.answers.lastOrNull()

                    val status = when (lastAnswer?.status) {
                        "check" -> CaseUiStatus.CHECKING
                        "ok" -> CaseUiStatus.DONE
                        else -> CaseUiStatus.OPEN
                    }

                    PracticumCaseUi(
                        id = case.id,
                        name = case.name,
                        description = case.description,
                        status = status,
                        adminComment = lastAnswer?.comment,
                        userAnswer = lastAnswer?.text
                    )
                }

                _cases.value = mapped

                // СЧЁТЧИК
                val done = mapped.count { it.status == CaseUiStatus.DONE }
                val notDone = mapped.count { it.status != CaseUiStatus.DONE }

                _stats.value = Pair(done, notDone)
            } catch (e: Exception) {

                _cases.value = emptyList()
                _stats.value = Pair(0, 0)
            }

        }
    }

    fun sendAnswer(caseId: Int, text: String) {
        viewModelScope.launch {

            updateCaseStatus(caseId, CaseUiStatus.CHECKING)

            repository.sendAnswer(caseId, text)

            loadCases()
        }
    }

    private fun updateCaseStatus(id: Int, status: CaseUiStatus) {
        _cases.value = _cases.value?.map {
            if (it.id == id) it.copy(status = status) else it
        }
    }
}