package com.mintlifescience.app.helperUtils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PrefsManager {

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            AppConstants.Prefs.FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isLoggedIn(context: Context) =
        prefs(context).getBoolean(AppConstants.Prefs.IS_LOGGED_IN, false)

    fun userId(context: Context) =
        prefs(context).getString(AppConstants.Prefs.USER_ID, null)

    fun userEmail(context: Context) =
        prefs(context).getString(AppConstants.Prefs.USER_EMAIL, null)

    fun userName(context: Context) =
        prefs(context).getString(AppConstants.Prefs.USER_NAME, null)

    fun saveLoginState(context: Context, userId: String, email: String) {
        prefs(context).edit()
            .putBoolean(AppConstants.Prefs.IS_LOGGED_IN, true)
            .putString(AppConstants.Prefs.USER_ID, userId)
            .putString(AppConstants.Prefs.USER_EMAIL, email)
            .apply()
    }

    fun saveUserName(context: Context, name: String) {
        prefs(context).edit()
            .putString(AppConstants.Prefs.USER_NAME, name)
            .apply()
    }

    fun clearSession(context: Context) {
        prefs(context).edit()
            .putBoolean(AppConstants.Prefs.IS_LOGGED_IN, false)
            .remove(AppConstants.Prefs.USER_ID)
            .remove(AppConstants.Prefs.USER_EMAIL)
            .remove(AppConstants.Prefs.USER_NAME)
            .apply()
    }
}
