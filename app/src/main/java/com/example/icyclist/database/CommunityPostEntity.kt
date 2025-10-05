package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userEmail: String,
    val userNickname: String,
    val userAvatarPath: String?,
    
    val content: String,
    val thumbnailPath: String?,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    // 可选:关联的运动记录ID
    val sportRecordId: Long? = null
)
