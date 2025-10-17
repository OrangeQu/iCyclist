package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 点赞实体
 * 用于存储帖子的点赞信息
 */
@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val postId: Int,              // 关联的帖子ID
    val userId: String,           // 点赞者ID（用户邮箱）
    val timestamp: Long = System.currentTimeMillis()  // 点赞时间
)


