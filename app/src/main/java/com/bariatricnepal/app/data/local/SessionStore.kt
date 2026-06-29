package com.bariatricnepal.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple SharedPreferences-backed store. No Room/DataStore needed for the
 * small amount of state this app keeps locally (server URL + session
 * token + a few cached profile fields for instant UI on launch).
 */
class SessionStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("bn_session", Context.MODE_PRIVATE)

    var baseUrl: String?
        get() = prefs.getString(KEY_BASE_URL, null)
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var patientId: String?
        get() = prefs.getString(KEY_PATIENT_ID, null)
        set(value) = prefs.edit().putString(KEY_PATIENT_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        set(value) = prefs.edit().putString(KEY_FULL_NAME, value).apply()

    var profilePicture: String?
        get() = prefs.getString(KEY_PROFILE_PIC, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_PIC, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    val isServerConfigured: Boolean
        get() = !baseUrl.isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_PATIENT_ID)
            .remove(KEY_FULL_NAME)
            .remove(KEY_PROFILE_PIC)
            .apply()
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_PATIENT_ID = "patient_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PROFILE_PIC = "profile_picture"
    }
}
