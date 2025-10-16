package com.example.icyclist.network.model.forum

import com.example.icyclist.network.model.User

data class ForumTopic(
    val id: Long? = null,
    val categoryId: Long = 0,
    val userId: Long = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val user: User? = null,
    val replies: List<ForumReply>? = null
)

