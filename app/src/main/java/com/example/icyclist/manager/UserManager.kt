package com.example.icyclist.manager

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
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    
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
    
    /**
     * 设置登录状态（登录成功后调用）
     */
    fun setLoggedIn(context: Context, email: String, nickname: String? = null) {
        getPreferences(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER_EMAIL, email)
            .apply()
        
        // 如果提供了昵称，也保存
        nickname?.let {
            getPreferences(context).edit()
                .putString("$KEY_NICKNAME_PREFIX$email", it)
                .apply()
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
    
    // ==================== JWT Token 相关方法 ====================
    
    /**
     * 保存认证 Token（登录成功后调用）
     */
    fun saveAuthToken(context: Context, token: String) {
        getPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }
    
    /**
     * 获取认证 Token
     */
    fun getAuthToken(context: Context): String? {
        return getPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * 清除认证 Token（退出登录时调用）
     */
    fun clearAuthToken(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    /**
     * 保存用户 ID
     */
    fun saveUserId(context: Context, userId: Long) {
        getPreferences(context).edit()
            .putLong(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * 获取用户 ID
     */
    fun getUserId(context: Context): Long? {
        val prefs = getPreferences(context)
        return if (prefs.contains(KEY_USER_ID)) {
            prefs.getLong(KEY_USER_ID, -1)
        } else {
            null
        }
    }
}
