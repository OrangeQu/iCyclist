package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LikeDao {

    /**
     * 插入点赞
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    /**
     * 根据帖子ID和用户ID删除点赞（取消点赞）
     */
    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: Int, userId: String)

    /**
     * 检查用户是否已点赞某个帖子
     */
    @Query("SELECT COUNT(*) > 0 FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun isLiked(postId: Int, userId: String): Boolean

    /**
     * 获取某个帖子的点赞数量
     */
    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    suspend fun getLikeCount(postId: Int): Int

    /**
     * 获取某个帖子的所有点赞记录
     */
    @Query("SELECT * FROM likes WHERE postId = :postId ORDER BY timestamp DESC")
    suspend fun getLikesByPostId(postId: Int): List<LikeEntity>

    /**
     * 根据帖子ID删除所有点赞（删除帖子时使用）
     */
    @Query("DELETE FROM likes WHERE postId = :postId")
    suspend fun deleteLikesByPostId(postId: Int)
}


