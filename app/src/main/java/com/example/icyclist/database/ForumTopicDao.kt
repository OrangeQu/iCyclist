package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ForumTopicDao {

    /**
     * 插入主题
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: ForumTopicEntity): Long

    /**
     * 根据分类ID获取主题列表
     */
    @Query("SELECT * FROM forum_topics WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    suspend fun getTopicsByCategory(categoryId: Int): List<ForumTopicEntity>

    /**
     * 根据ID获取主题
     */
    @Query("SELECT * FROM forum_topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Int): ForumTopicEntity?

    /**
     * 获取所有主题
     */
    @Query("SELECT * FROM forum_topics ORDER BY timestamp DESC")
    suspend fun getAllTopics(): List<ForumTopicEntity>

    /**
     * 更新主题
     */
    @Update
    suspend fun updateTopic(topic: ForumTopicEntity)

    /**
     * 删除主题
     */
    @Delete
    suspend fun deleteTopic(topic: ForumTopicEntity)

    /**
     * 增加回复计数
     */
    @Query("UPDATE forum_topics SET replyCount = replyCount + 1 WHERE id = :topicId")
    suspend fun incrementReplyCount(topicId: Int)

    /**
     * 减少回复计数
     */
    @Query("UPDATE forum_topics SET replyCount = replyCount - 1 WHERE id = :topicId AND replyCount > 0")
    suspend fun decrementReplyCount(topicId: Int)

    /**
     * 获取指定分类的主题数量
     */
    @Query("SELECT COUNT(*) FROM forum_topics WHERE categoryId = :categoryId")
    suspend fun getTopicCountByCategory(categoryId: Int): Int
}


