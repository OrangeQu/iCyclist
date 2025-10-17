package com.icyclist.server.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRegistrationRequest(
    @JsonProperty("email")  // 客户端发送email字段
    val username: String,   // 在服务器内部称为username（作为用户登录名）
    val password: String,
    val nickname: String? = null
)






















