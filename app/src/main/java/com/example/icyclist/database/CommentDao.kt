package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommentDao {

    /**
     * 插入评论
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    /**
     * 根据帖子ID获取所有评论
     */
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    suspend fun getCommentsByPostId(postId: Int): List<CommentEntity>

    /**
     * 获取某个帖子的评论数量
     */
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCount(postId: Int): Int

    /**
     * 删除评论
     */
    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    /**
     * 根据帖子ID删除所有评论（删除帖子时使用）
     */
    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: Int)
}


