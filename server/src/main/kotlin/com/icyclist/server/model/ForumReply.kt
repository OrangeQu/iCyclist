package com.icyclist.server.model

import java.sql.Timestamp

data class ForumReply(
    var id: Long? = null,
    var topicId: Long = 0,
    var userId: Long = 0,
    var content: String = "",
    var createdAt: Timestamp? = null,

    // Aggregated data
    var user: User? = null
)














