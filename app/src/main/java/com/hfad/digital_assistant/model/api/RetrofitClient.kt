package com.hfad.digital_assistant.model.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://methodical-space.ru/"

    private fun getRetrofit(userPreferences: UserPreferences): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { userPreferences.getToken() })
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(userPreferences: UserPreferences): AuthApiService {
        return getRetrofit(userPreferences).create(AuthApiService::class.java)
    }

    fun createMonitoringApi(userPreferences: UserPreferences): MonitoringApiService {
        return getRetrofit(userPreferences).create(MonitoringApiService::class.java)
    }
}