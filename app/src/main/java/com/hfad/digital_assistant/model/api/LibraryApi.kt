package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.LibraryFile
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface LibraryApi {
    @GET("files/")
    suspend fun getFiles(): List<LibraryFile>

    @GET("files/{slug}/")
    suspend fun getFile(@Path("slug") slug: String): LibraryFile

    companion object {
        fun create(): LibraryApi {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://methodical-space.ru/api/library/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(LibraryApi::class.java)
        }
    }
}