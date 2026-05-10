package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.CurrentApi

class RemoteCurrentRepository(
    private val api: CurrentApi
) {
    suspend fun getCurrentGoal() = api.getCurrentGoal()

    suspend fun setCurrentGoal(goal: String) =
        api.setCurrentGoal(GoalRequest(text = goal))

    suspend fun deleteCurrentGoal() =
        api.deleteCurrentGoal()

    suspend fun getRandomQuote() =
        api.getRandomQuote()
}