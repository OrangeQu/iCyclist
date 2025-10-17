package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ForumCategoryDao {

    /**
     * 插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ForumCategoryEntity)

    /**
     * 批量插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ForumCategoryEntity>)

    /**
     * 获取所有分类
     */
    @Query("SELECT * FROM forum_categories ORDER BY id ASC")
    suspend fun getAllCategories(): List<ForumCategoryEntity>

    /**
     * 根据ID获取分类
     */
    @Query("SELECT * FROM forum_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): ForumCategoryEntity?

    /**
     * 更新分类
     */
    @Update
    suspend fun updateCategory(category: ForumCategoryEntity)

    /**
     * 增加主题计数
     */
    @Query("UPDATE forum_categories SET topicCount = topicCount + 1 WHERE id = :categoryId")
    suspend fun incrementTopicCount(categoryId: Int)

    /**
     * 减少主题计数
     */
    @Query("UPDATE forum_categories SET topicCount = topicCount - 1 WHERE id = :categoryId AND topicCount > 0")
    suspend fun decrementTopicCount(categoryId: Int)
}


