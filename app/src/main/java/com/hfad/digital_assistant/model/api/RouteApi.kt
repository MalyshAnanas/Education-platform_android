package com.hfad.digital_assistant.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RouteApi {

    @GET("/api/route/modules/")
    suspend fun getModules(): List<Module>

    object RouteApiFactory {
        fun create(userPreferences: UserPreferences): RouteApi {
            return Retrofit.Builder()
                .baseUrl("https://methodical-space.ru/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RouteApi::class.java)
        }
    }
}