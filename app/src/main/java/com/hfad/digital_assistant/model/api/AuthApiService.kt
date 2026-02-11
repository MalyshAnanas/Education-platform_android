package com.hfad.digital_assistant.model.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

//это «план» того, как мобильное приложение будет общаться с сервером.

/**
 * Интерфейс для взаимодействия с API аутентификации и получения данных.
 * Определяет методы для выполнения HTTP-запросов, таких, как получение страницы входа,
 * аутентификация пользователя и получение страницы статистики посещений.
 */
interface AuthApiService {

    /**
     * Вход в систему
     * POST /api/users/login/
     * Тело запроса: JSON { username, password }
     * Ответ: { token }
     */
    @POST("/api/users/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Выход из системы
     * POST /api/users/logout/
     * Требует заголовок Authorization
     */
    @POST("/api/users/logout/")
    suspend fun logout(): Response<Unit>

    /**
     * Получение данных текущего пользователя
     * GET /api/users/me/
     */
    @GET("/api/users/me/")
    suspend fun getMe(): Response<UserDto>

    @POST("/api/users/register/")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserDto>


}
