package com.example.icyclist.network.model

data class Comment(
    val id: Long? = null,
    val postId: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val createdAt: String? = null,
    val user: User? = null
)

