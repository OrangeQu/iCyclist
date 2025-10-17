package com.example.icyclist.network.model.forum

import com.example.icyclist.network.model.User

data class ForumReply(
    val id: Long? = null,
    val topicId: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val createdAt: String? = null,
    val user: User? = null
) {
    // 辅助属性，方便访问
    val authorName: String get() = user?.nickname ?: user?.username ?: "匿名"
    val authorAvatar: String get() = user?.avatar ?: ""
}

