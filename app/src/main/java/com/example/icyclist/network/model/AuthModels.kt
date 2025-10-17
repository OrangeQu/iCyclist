package com.example.icyclist.network.model

/**
 * 用户注册请求
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String?
)

/**
 * 用户登录请求
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * 登录响应
 */
data class LoginResponse(
    val token: String,
    val user: User? = null
)

/**
 * 注册响应
 */
data class RegisterResponse(
    val user: User
)

/**
 * 用户资料请求
 */
data class ProfileRequest(
    val nickname: String?,
    val avatar: String?
)

/**
 * 用户资料响应
 */
data class ProfileResponse(
    val id: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val createdAt: String?
)

