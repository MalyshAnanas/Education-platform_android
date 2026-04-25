package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.api.CurrentIndicatorDto
import com.hfad.digital_assistant.model.api.IndicatorUpdateItemDto
import com.hfad.digital_assistant.model.api.HistoryPeriodDto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MonitoringApiService {

    @GET("/api/monitoring/indicators/current/")
    suspend fun getCurrentIndicators(): Response<List<CurrentIndicatorDto>>

    @PATCH("/api/monitoring/indicators/current/")
    suspend fun sendIndicators(
        @Body answers: List<IndicatorUpdateItemDto>
    ): Response<Unit>

    @GET("/api/monitoring/indicators/history/")
    suspend fun getHistory(): List<HistoryPeriodDto>
}