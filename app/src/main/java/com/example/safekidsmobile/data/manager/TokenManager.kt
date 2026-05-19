package com.example.safekidsmobile.data.manager

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedSharedPreferences.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return encryptedSharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    fun saveUserInfo(userId: String, email: String, fullName: String) {
        encryptedSharedPreferences.edit().apply {
            putString(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, email)
            putString(USER_FULL_NAME_KEY, fullName)
            apply()
        }
    }

    fun getUserInfo(): Triple<String?, String?, String?>? {
        val userId = encryptedSharedPreferences.getString(USER_ID_KEY, null)
        val email = encryptedSharedPreferences.getString(USER_EMAIL_KEY, null)
        val fullName = encryptedSharedPreferences.getString(USER_FULL_NAME_KEY, null)

        return if (userId != null && email != null) {
            Triple(userId, email, fullName)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun clearTokens() {
        encryptedSharedPreferences.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove(USER_ID_KEY)
            remove(USER_EMAIL_KEY)
            remove(USER_FULL_NAME_KEY)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "safekid_secure_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_FULL_NAME_KEY = "user_full_name"
    }
}
