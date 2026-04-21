package com.hfad.digital_assistant.model.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

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
     * Регистрация
     * POST /api/users/register/
     * Тело запроса: JSON { username, password, email, full_name }
     */
    @POST("/api/users/register/")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserDto>

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

    /**
     * Обновление данных профиля текущего пользователя
     * PATCH /api/users/me/
     */
    @PATCH("/api/users/me/")
    suspend fun updateMe(
        @Body body: UpdateUserRequest
    ): Response<UserDto>
}