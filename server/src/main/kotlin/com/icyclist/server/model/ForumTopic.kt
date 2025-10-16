package com.icyclist.server.model

import java.sql.Timestamp

data class ForumTopic(
    var id: Long? = null,
    var categoryId: Long = 0,
    var userId: Long = 0,
    var title: String = "",
    var content: String = "",
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,

    // Aggregated data
    var user: User? = null,
    var replies: List<ForumReply>? = null
)














