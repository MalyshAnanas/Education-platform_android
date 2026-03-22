package com.hfad.digital_assistant.model.local

import com.hfad.digital_assistant.model.api.AuthInterceptor
import com.hfad.digital_assistant.model.api.GoalRequest
import com.hfad.digital_assistant.model.api.GoalResponse
import com.hfad.digital_assistant.model.api.QuoteResponse
import com.hfad.digital_assistant.model.api.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface CurrentApi {

    companion object {
        fun create(userPreferences: UserPreferences): CurrentApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor { userPreferences.getToken() })
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://methodical-space.ru/api/main/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(CurrentApi::class.java)
        }
    }

    // Получить цель
    @GET("current/")
    suspend fun getCurrentGoal(): Response<GoalResponse>

    // Отправить цель
    @POST("current/")
    suspend fun setCurrentGoal(@Body request: GoalRequest): Response<Unit>

    // Удалить цель
    @DELETE("current/")
    suspend fun deleteCurrentGoal(): Response<Unit>

    // Получить случайную цитату
    @GET("random-quote/")
    suspend fun getRandomQuote(): Response<QuoteResponse>
}