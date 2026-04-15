package com.hfad.digital_assistant.model.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PracticumApi {

    @GET("practicum/open-cases/")
    suspend fun getOpenCases(): List<PracticumCase>

    @POST("practicum/answer/")
    suspend fun sendAnswer(
        @Body request: PracAnswerRequest
    ): PracAnswerResponse

    @GET("practicum/closed-cases/")
    suspend fun getClosedCases(): List<PracticumCase>
}