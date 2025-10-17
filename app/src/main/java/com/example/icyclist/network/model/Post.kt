package com.example.icyclist.network.model

data class Post(
    val id: Long? = null,
    val userId: Long = 0,
    val rideRecordId: Long? = null,
    val content: String? = null,
    val mediaUrls: List<String>? = null,
    val createdAt: String? = null,
    val user: User? = null,
    val likes: List<PostLike>? = null,
    val comments: List<Comment>? = null,
    var isLiked: Boolean = false  // 前端用于标记当前用户是否已点赞
) {
    // 辅助属性，方便访问
    val authorName: String get() = user?.nickname ?: user?.username ?: "骑行者"
    val authorAvatar: String get() = user?.avatar ?: ""
    val imageUrls: List<String>? get() = mediaUrls
    val likeCount: Int get() = likes?.size ?: 0
    val commentCount: Int get() = comments?.size ?: 0
}

