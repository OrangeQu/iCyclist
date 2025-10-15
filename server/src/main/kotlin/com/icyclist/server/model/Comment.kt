package com.icyclist.server.model

import java.sql.Timestamp

data class Comment(
    var id: Long? = null,
    var postId: Long = 0,
    var userId: Long = 0,
    var content: String = "",
    var createdAt: Timestamp? = null,
    var user: User? = null
)




