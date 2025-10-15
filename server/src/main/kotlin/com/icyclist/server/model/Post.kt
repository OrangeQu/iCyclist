package com.icyclist.server.model

import java.sql.Timestamp

data class Post(
    var id: Long? = null,
    var userId: Long = 0,
    var rideRecordId: Long? = null,
    var content: String? = null,
    var mediaUrls: List<String>? = null,
    var createdAt: Timestamp? = null,
    var user: User? = null,
    var likes: List<PostLike>? = null,
    var comments: List<Comment>? = null
)




