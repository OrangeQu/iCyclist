package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 论坛主题实体
 * 用于存储论坛的主题（帖子）信息
 */
@Entity(tableName = "forum_topics")
data class ForumTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val categoryId: Int,          // 所属分类ID
    val userId: String,           // 作者ID（用户邮箱）
    val userNickname: String,     // 作者昵称
    val userAvatar: String,       // 作者头像
    val title: String,            // 主题标题
    val content: String,          // 主题内容
    val timestamp: Long = System.currentTimeMillis(),  // 创建时间
    val replyCount: Int = 0       // 回复数量
)


