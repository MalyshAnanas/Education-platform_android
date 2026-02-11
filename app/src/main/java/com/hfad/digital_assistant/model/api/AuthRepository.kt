package com.hfad.digital_assistant.model.api

import android.util.Log
import com.hfad.digital_assistant.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userPreferences: UserPreferences,
    private val authApiService: AuthApiService
) {
    private val TAG = "AuthRepository"


    /** Локальная проверка */
    fun isAuthorized(): Boolean =
        userPreferences.getToken() != null

    fun getSavedUser(): UserData? {
        val fullName = userPreferences.getFullName() ?: return null
        return UserData(fullName)
    }

    /** Логин */
    suspend fun login(username: String, password: String): Result<UserData> =
        withContext(Dispatchers.IO) {

            if (username.isBlank() || password.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Введите логин и пароль")
                )
            }

            try {
                // 1. Логин → получаем токен
                val loginResponse = authApiService.login(
                    LoginRequest(username, password)
                )
                Log.d(TAG, "login(): response code = ${loginResponse.code()}")

                if (!loginResponse.isSuccessful) {
                    Log.e(TAG, "login(): error body = ${loginResponse.errorBody()?.string()}")
                    return@withContext Result.failure(
                        Exception("Неверный логин или пароль")
                    )
                }

                val token = loginResponse.body()?.token
                Log.d(TAG, "login(): token from server = $token")

                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(
                        Exception("Сервер не вернул токен")
                    )
                }

                // 2. Сохраняем токен
                userPreferences.saveToken(token)
                Log.d(TAG, "login(): token saved to preferences")

                // 3. Получаем профиль пользователя
                val meResponse = authApiService.getMe()
                if (!meResponse.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Не удалось получить профиль")
                    )
                }

                val user = meResponse.body()
                    ?: return@withContext Result.failure(
                        Exception("Пустой профиль пользователя")
                    )

                val fullName = user.profile.full_name ?: user.username

                userPreferences.saveUser(
                    fullName = fullName,
                    username = user.username
                )

                Result.success(UserData(fullName))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Проверка токена */
    suspend fun checkSession(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                authApiService.getMe().isSuccessful
            } catch (e: Exception) {
                false
            }
        }

    /** Выход */
    suspend fun logout() =
        withContext(Dispatchers.IO) {
            try {
                authApiService.logout()
            } catch (_: Exception) {
                // игнорируем
            } finally {
                userPreferences.clear()
            }
        }

    suspend fun tryAutoLogin(): Result<UserData> =
        withContext(Dispatchers.IO) {

            val token = userPreferences.getToken()
                ?: return@withContext Result.failure(Exception("No token"))

            try {
                val response = authApiService.getMe()

                if (!response.isSuccessful) {
                    userPreferences.clear()
                    return@withContext Result.failure(Exception("Token expired"))
                }

                val user = response.body()
                    ?: return@withContext Result.failure(Exception("Empty user"))

                val fullName = user.profile.full_name ?: user.username

                userPreferences.saveUser(
                    fullName = fullName,
                    username = user.username
                )

                Result.success(UserData(fullName))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun register(
        username: String,
        password: String,
        email: String,
        fullName: String
    ): Result<UserData> =
        withContext(Dispatchers.IO) {

            try {
                val response = authApiService.register(
                    RegisterRequest(
                        username = username,
                        password = password,
                        email = email,
                        full_name = fullName
                    )
                )

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Ошибка регистрации")
                    )
                }

                Result.success(UserData(fullName))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}
