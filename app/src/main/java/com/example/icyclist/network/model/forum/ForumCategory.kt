package com.example.icyclist.network.model.forum

data class ForumCategory(
    val id: Long? = null,
    val name: String = "",
    val description: String? = null,
    val createdAt: String? = null,
    var topicCount: Int = 0  // 前端计算或后端提供
)

