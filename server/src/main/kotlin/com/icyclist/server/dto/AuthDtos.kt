package com.icyclist.server.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.icyclist.server.model.User

data class LoginRequest(
    @JsonProperty("email")  // 客户端发送email字段
    val username: String,   // 在服务器内部称为username
    val password: String
)

data class LoginResponse(
    val token: String,  // 改为token以匹配客户端
    val user: User? = null,  // 返回用户信息
    val tokenType: String = "Bearer"
)






















