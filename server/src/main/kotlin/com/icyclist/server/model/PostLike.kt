package com.icyclist.server.model

import java.sql.Timestamp

data class PostLike(
    var postId: Long,
    var userId: Long,
    var createdAt: Timestamp? = null
)





