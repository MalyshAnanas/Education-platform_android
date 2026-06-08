package com.hfad.digital_assistant.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.model.api.AuthInterceptor
import com.hfad.digital_assistant.model.api.PracticumApi
import com.hfad.digital_assistant.model.api.PracticumRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.local.AppDatabase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PracticumViewModelFactory(
    private val context: Context,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { userPreferences.getToken() })
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://methodical-space.ru/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(PracticumApi::class.java)

        val db = AppDatabase.getDatabase(context)

        val dao = db.practicumDao()

        val repo = PracticumRepository(
            api = api,
            dao = dao
        )

        return PracticumViewModel(repo, userPreferences) as T
    }
}