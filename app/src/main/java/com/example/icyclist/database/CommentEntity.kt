package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 评论实体
 * 用于存储帖子的评论信息
 */
@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val postId: Int,              // 关联的帖子ID
    val userId: String,           // 评论者ID（用户邮箱）
    val userNickname: String,     // 评论者昵称
    val userAvatar: String,       // 评论者头像
    val content: String,          // 评论内容
    val timestamp: Long = System.currentTimeMillis()  // 评论时间
)


