package com.hfad.digital_assistant.model.api

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = // TODO сделать шифрование
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /* ================= TOKEN ================= */

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    /* ================= USER ================= */

    fun saveUser(fullName: String, username: String?) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getFullName(): String? =
        prefs.getString(KEY_FULL_NAME, null)

    fun getUsername(): String? =
        prefs.getString(KEY_USERNAME, null)

    /* ================= AUTH ================= */

    fun isAuthorized(): Boolean {
        return !getToken().isNullOrEmpty()
    }


    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "user_preferences"

        private const val KEY_TOKEN = "auth_token"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_USERNAME = "username"
    }
}
