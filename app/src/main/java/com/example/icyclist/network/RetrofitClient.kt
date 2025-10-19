package com.example.icyclist.network

import android.content.Context
import com.example.icyclist.manager.UserManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络请求客户端
 * 单例模式，提供统一的网络请求配置
 */
object RetrofitClient {

    /**
     * 服务器基础 URL
     */
    private const val BASE_URL = "http://192.168.81.39:8080/"

    /**
     * 日志拦截器 - 用于开发调试
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 认证拦截器 - 自动添加 JWT Token 到请求头
     */
    private class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            
            // 从 UserManager 获取 JWT Token
            val token = UserManager.getAuthToken(context)
            
            // 如果有 token，添加到请求头
            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            
            return chain.proceed(newRequest)
        }
    }

    /**
     * OkHttpClient 配置
     */
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)  // 添加日志拦截器
            .addInterceptor(AuthInterceptor(context))  // 添加认证拦截器
            .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(30, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
            .build()
    }


    private var retrofit: Retrofit? = null

    /**
     * 初始化 Retrofit
     */
    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * 获取 ApiService 实例
     */
    fun getApiService(context: Context): ApiService {
        return getRetrofit(context).create(ApiService::class.java)
    }

    /**
     * 更新基础 URL
     */
    fun updateBaseUrl(newBaseUrl: String, context: Context) {
        retrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(getOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

