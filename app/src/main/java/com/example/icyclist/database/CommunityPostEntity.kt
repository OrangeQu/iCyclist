package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userAvatar: String, // 这里可以存一个 drawable 名字或者未来的 URL
    val userNickname: String,
    val content: String,
    val imageUrl: String?, // 帖子图片路径，可以为空
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comments: Int = 0,
    
    // 关联的运动记录信息 (可选)
    val sportRecordId: Long? = null,
    val sportDistance: String? = null,
    val sportDuration: String? = null,
    val sportThumbPath: String? = null
)
