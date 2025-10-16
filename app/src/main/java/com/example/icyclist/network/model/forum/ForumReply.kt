package com.example.icyclist.network.model.forum

import com.example.icyclist.network.model.User

data class ForumReply(
    val id: Long? = null,
    val topicId: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val createdAt: String? = null,
    val user: User? = null
)

