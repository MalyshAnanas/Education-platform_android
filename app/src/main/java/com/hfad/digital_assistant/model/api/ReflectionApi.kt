package com.hfad.digital_assistant.model.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface ReflectionApi {

    @GET("reflection/")
    suspend fun getModules(): List<Module>

    @GET("reflection/questions/")
    suspend fun getQuestions(): List<Question>

    @GET("reflection/answers-history/")
    suspend fun getAnswersHistory(): List<QuestionHistory>

    @POST("reflection/answer/")
    suspend fun sendAnswers(@Body request: AnswerBulkRequest): Response<List<Answer>>

    object ReflectionApiFactory {

        fun create(userPreferences: UserPreferences): ReflectionApi {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor { userPreferences.getToken() })
                .build()

            return Retrofit.Builder()
                .baseUrl("https://methodical-space.ru/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ReflectionApi::class.java)
        }
    }
}