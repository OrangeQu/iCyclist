package com.example.icyclist

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object UserManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
    private const val KEY_USER_PREFIX = "user_"
    private const val KEY_NICKNAME_PREFIX = "nickname_"
    private const val KEY_AVATAR_PREFIX = "avatar_"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to normal SharedPreferences if encryption fails
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    fun register(context: Context, email: String, password: String, nickname: String): Boolean {
        val prefs = getPreferences(context)
        val userKey = "$KEY_USER_PREFIX$email"
        
        if (prefs.contains(userKey)) {
            return false // User already exists
        }
        
        prefs.edit()
            .putString(userKey, password)
            .putString("$KEY_NICKNAME_PREFIX$email", nickname)
            .apply()
        return true
    }
    
    fun login(context: Context, email: String, password: String): Boolean {
        val prefs = getPreferences(context)
        val userKey = "$KEY_USER_PREFIX$email"
        val storedPassword = prefs.getString(userKey, null)
        
        return if (storedPassword == password) {
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_CURRENT_USER_EMAIL, email)
                .apply()
            true
        } else {
            false
        }
    }
    
    fun logout(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_CURRENT_USER_EMAIL)
            .apply()
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_CURRENT_USER_EMAIL, null)
    }
    
    fun getCurrentUserNickname(context: Context): String? {
        val email = getCurrentUserEmail(context) ?: return null
        return getPreferences(context).getString("$KEY_NICKNAME_PREFIX$email", null)
    }
    
    fun updateNickname(context: Context, nickname: String) {
        val email = getCurrentUserEmail(context) ?: return
        getPreferences(context).edit()
            .putString("$KEY_NICKNAME_PREFIX$email", nickname)
            .apply()
    }
    
    fun getCurrentUserAvatar(context: Context): String? {
        val email = getCurrentUserEmail(context) ?: return null
        return getPreferences(context).getString("$KEY_AVATAR_PREFIX$email", null)
    }
    
    fun updateAvatar(context: Context, avatarPath: String) {
        val email = getCurrentUserEmail(context) ?: return
        getPreferences(context).edit()
            .putString("$KEY_AVATAR_PREFIX$email", avatarPath)
            .apply()
    }
}
