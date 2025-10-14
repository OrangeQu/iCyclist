package com.example.icyclist.community

import java.io.Serializable
import java.util.Date

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Date,
    val likes: Int = 0,
    val comments: Int = 0,
    val isLiked: Boolean = false
): Serializable