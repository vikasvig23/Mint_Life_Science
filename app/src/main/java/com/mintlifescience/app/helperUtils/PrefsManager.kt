package com.mintlifescience.app.helperUtils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PrefsManager {

    @Volatile
    private var instance: SharedPreferences? = null

    // Build EncryptedSharedPreferences once and cache it — rebuilding on every call is expensive
    private fun prefs(context: Context): SharedPreferences =
        instance ?: synchronized(this) {
            instance ?: run {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context.applicationContext,
                    AppConstants.Prefs.FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ).also { instance = it }
            }
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
            .remove(AppConstants.Prefs.IS_LOGGED_IN)
            .remove(AppConstants.Prefs.USER_ID)
            .remove(AppConstants.Prefs.USER_EMAIL)
            .remove(AppConstants.Prefs.USER_NAME)
            .apply()
    }
}
