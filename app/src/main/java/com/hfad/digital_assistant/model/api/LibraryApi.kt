package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.LibraryFile
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface LibraryApi {
    @GET("files/")
    suspend fun getFiles(): List<LibraryFile>

    @GET("files/{slug}/")
    suspend fun getFile(@Path("slug") slug: String): LibraryFile

    companion object {
        fun create(userPreferences: UserPreferences): LibraryApi {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor { userPreferences.getToken() })
                    .addInterceptor(logging)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://methodical-space.ru/api/library/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                return retrofit.create(LibraryApi::class.java)
        }
    }

    @Multipart
    @POST("files/")
    suspend fun uploadFile(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): LibraryFile
}