package com.icyclist.server.model

import java.sql.Timestamp

data class User(
    var id: Long? = null,
    var username: String = "",
    var passwordHash: String = "",
    var nickname: String? = null,
    var avatar: String? = null,
    var gender: Int? = 0,
    var bio: String? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
)
















