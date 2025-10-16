package com.example.icyclist.network.model

data class User(
    val id: Long? = null,
    val username: String = "",
    val nickname: String? = null,
    val avatar: String? = null,
    val gender: Int? = 0,
    val bio: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

