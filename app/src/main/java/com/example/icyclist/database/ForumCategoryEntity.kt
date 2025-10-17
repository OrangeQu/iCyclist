package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 论坛分类实体
 * 用于存储论坛的分类信息
 */
@Entity(tableName = "forum_categories")
data class ForumCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val name: String,             // 分类名称
    val description: String,      // 分类描述
    val topicCount: Int = 0       // 主题数量
)


