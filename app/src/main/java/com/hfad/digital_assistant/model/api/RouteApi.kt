package com.hfad.digital_assistant.model.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RouteApi {

    @GET("modules/")
    suspend fun getModules(): List<Module>

    object RouteApiFactory {
        fun create(userPreferences: UserPreferences): RouteApi {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor { userPreferences.getToken() })
                .build()

            return Retrofit.Builder()
                .baseUrl("https://methodical-space.ru/api/route/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RouteApi::class.java)
        }
    }

    @POST("/api/route/modules/{id}/completion/")
    suspend fun completeModule(@Path("id") id: Int)

    @DELETE("/api/route/modules/{id}/completion/")
    suspend fun uncompleteModule(@Path("id") id: Int)

    @GET("/api/route/modules/completed/")
    suspend fun getCompletedModules(): List<ModuleCompletion>

}