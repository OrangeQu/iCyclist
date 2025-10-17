package com.icyclist.server.model

import java.sql.Timestamp

data class ForumCategory(
    var id: Long? = null,
    var name: String = "",
    var description: String? = null,
    var createdAt: Timestamp? = null
)
















