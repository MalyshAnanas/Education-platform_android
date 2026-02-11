package com.hfad.digital_assistant.model.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

//гарантирует, что каждый раз, когда ваше приложение отправляет письмо (запрос) на сервер,
// на конверт будет наклеен специальный защитный код (CSRF-токен).

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()

        Log.d(TAG, "intercept(): token = $token")

        val request = if (token != null) {
            Log.d(TAG, "intercept(): Authorization header added")
            chain.request().newBuilder()
                .addHeader("Authorization", "Token $token")
                .build()
        } else {
            Log.w(TAG, "intercept(): token is NULL, request without auth")
            chain.request()
        }

        return chain.proceed(request)
    }
}
