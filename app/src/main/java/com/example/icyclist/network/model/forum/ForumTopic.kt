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
) {
    // 辅助属性，方便访问
    val authorName: String get() = user?.nickname ?: user?.username ?: "匿名"
    val authorAvatar: String get() = user?.avatar ?: ""
    val authorId: Long get() = userId
    val replyCount: Int get() = replies?.size ?: 0
}

