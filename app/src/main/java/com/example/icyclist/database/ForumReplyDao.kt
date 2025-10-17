package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ForumReplyDao {

    /**
     * 插入回复
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ForumReplyEntity): Long

    /**
     * 根据主题ID获取所有回复
     */
    @Query("SELECT * FROM forum_replies WHERE topicId = :topicId ORDER BY timestamp ASC")
    suspend fun getRepliesByTopicId(topicId: Int): List<ForumReplyEntity>

    /**
     * 获取某个主题的回复数量
     */
    @Query("SELECT COUNT(*) FROM forum_replies WHERE topicId = :topicId")
    suspend fun getReplyCount(topicId: Int): Int

    /**
     * 删除回复
     */
    @Delete
    suspend fun deleteReply(reply: ForumReplyEntity)

    /**
     * 根据主题ID删除所有回复（删除主题时使用）
     */
    @Query("DELETE FROM forum_replies WHERE topicId = :topicId")
    suspend fun deleteRepliesByTopicId(topicId: Int)
}


