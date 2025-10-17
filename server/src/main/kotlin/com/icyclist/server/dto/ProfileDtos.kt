package com.icyclist.server.dto

data class ProfileRequest(
    val nickname: String?,
    val avatar: String?
)

data class ProfileResponse(
    val id: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val createdAt: String?
)

