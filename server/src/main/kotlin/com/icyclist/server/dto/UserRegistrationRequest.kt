package com.icyclist.server.dto

data class UserRegistrationRequest(
    val username: String,
    val password: String,
    val nickname: String? = null
)







