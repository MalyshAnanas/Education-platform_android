package com.hfad.digital_assistant.model.api

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
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

    fun saveUser(
        fullName: String,
        username: String?,
        position: String? = null,
        organization: String? = null,
        email: String? = null
    ) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_USERNAME, username)
            .putString(KEY_POSITION, position)
            .putString(KEY_ORGANIZATION, organization)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getFullName(): String? =
        prefs.getString(KEY_FULL_NAME, null)

    fun getUsername(): String? =
        prefs.getString(KEY_USERNAME, null)

    fun getPosition(): String? =
        prefs.getString(KEY_POSITION, null)

    fun getOrganization(): String? =
        prefs.getString(KEY_ORGANIZATION, null)

    fun getEmail(): String? =
        prefs.getString(KEY_EMAIL, null)

    fun savePhotoUri(uri: String) {
        prefs.edit().putString(KEY_PHOTO_URI, uri).apply()
    }

    fun getPhotoUri(): String? = prefs.getString(KEY_PHOTO_URI, null)

    fun clearPhotoUri() {
        prefs.edit().remove(KEY_PHOTO_URI).apply()
    }

    /* ================= AUTH ================= */

    fun isAuthorized(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    /* ================= Цели ================= */

    fun saveGoal(goal: String) {
        prefs.edit().putString("goal", goal).apply()
    }

    fun getGoal(): String? {
        return prefs.getString("goal", null)
    }

    fun clearGoal() {
        prefs.edit().remove("goal").apply()
    }

    companion object {
        private const val PREFS_NAME = "user_preferences"

        private const val KEY_TOKEN = "auth_token"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_POSITION = "position"
        private const val KEY_ORGANIZATION = "organization"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHOTO_URI = "photo_uri"
    }
}