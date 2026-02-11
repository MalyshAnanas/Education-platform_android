package com.hfad.digital_assistant.model.api

//Вместо того чтобы передавать логин и пароль по отдельности в разные методы, мы упаковываем их в одну структуру LoginRequest

/**
 * Data class, представляющий тело запроса для аутентификации пользователя.
 * Используется для инкапсуляции логина и пароля, отправляемых на сервер во время входа в систему.
 *
 * @property username Имя пользователя (логин) для аутентификации.
 * @property password Пароль пользователя для аутентификации.
 */
data class LoginRequest(
    val username: String,
    val password: String
)