package com.example.icyclist.network.model

data class PostLike(
    val id: Long? = null,
    val postId: Long = 0,
    val userId: Long = 0,
    val createdAt: String? = null
)

