package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 论坛回复实体
 * 用于存储论坛主题的回复信息
 */
@Entity(tableName = "forum_replies")
data class ForumReplyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val topicId: Int,             // 所属主题ID
    val userId: String,           // 回复者ID（用户邮箱）
    val userNickname: String,     // 回复者昵称
    val userAvatar: String,       // 回复者头像
    val content: String,          // 回复内容
    val timestamp: Long = System.currentTimeMillis()  // 回复时间
)


