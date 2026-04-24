package com.hfad.digital_assistant.model.api

import android.util.Log
import com.hfad.digital_assistant.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
                    username = user.username,
                    position = user.profile.position,
                    organization = user.profile.organization,
                    email = user.email
                )

                userPreferences.saveServerPhotoUrl(user.profile.photo)

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
                    username = user.username,
                    position = user.profile.position,
                    organization = user.profile.organization,
                    email = user.email
                )

                userPreferences.saveServerPhotoUrl(user.profile.photo)

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

    /** Обновление профиля */
    suspend fun updateProfile(
        fullName: String,
        position: String,
        organization: String
    ): Result<UserData> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApiService.updateMe(
                    UpdateUserRequest(
                        profile = UpdateProfileRequest(
                            full_name = fullName,
                            position = position,
                            organization = organization
                        )
                    )
                )

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Не удалось обновить профиль")
                    )
                }

                val user = response.body()
                    ?: return@withContext Result.failure(
                        Exception("Пустой ответ сервера")
                    )

                val actualFullName = user.profile.full_name ?: user.username

                userPreferences.saveUser(
                    fullName = actualFullName,
                    username = user.username,
                    position = user.profile.position,
                    organization = user.profile.organization,
                    email = user.email
                )

                userPreferences.saveServerPhotoUrl(user.profile.photo)

                Result.success(UserData(actualFullName))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    /** Метод загрузки профиля */
    suspend fun getProfile(): Result<UserDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApiService.getMe()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Не удалось получить профиль")
                    )
                }

                val user = response.body()
                    ?: return@withContext Result.failure(
                        Exception("Пустой профиль пользователя")
                    )

                val fullName = user.profile.full_name ?: user.username

                userPreferences.saveUser(
                    fullName = fullName,
                    username = user.username,
                    position = user.profile.position,
                    organization = user.profile.organization,
                    email = user.email
                )

                userPreferences.saveServerPhotoUrl(user.profile.photo)

                Result.success(user)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Метод загрузки фото на сервер */
    suspend fun updatePhoto(
        context: Context,
        uri: Uri
    ): Result<UserDto> =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Не удалось открыть фото"))

                val bytes = inputStream.use { it.readBytes() }

                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                val photoPart = MultipartBody.Part.createFormData(
                    name = "photo",
                    filename = "profile_photo_${System.currentTimeMillis()}.jpg",
                    body = requestBody
                )

                val response = authApiService.updatePhoto(photoPart)

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Не удалось загрузить фото на сервер")
                    )
                }

                val user = response.body()
                    ?: return@withContext Result.failure(Exception("Пустой ответ сервера"))

                userPreferences.saveServerPhotoUrl(user.profile.photo)

                Result.success(user)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
