package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.api.MonitoringApiService
import com.hfad.digital_assistant.model.api.IndicatorUpdateItemDto


class MonitoringRepository(
    private val api: MonitoringApiService
) {
    suspend fun getCurrentIndicators() = api.getCurrentIndicators()

    suspend fun sendAnswers(answers: List<IndicatorUpdateItemDto>) =
        api.sendIndicators(answers)

    suspend fun getHistory() = api.getHistory()
}