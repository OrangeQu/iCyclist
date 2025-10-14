package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommunityPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    suspend fun getAllPosts(): List<CommunityPostEntity>

    @Delete
    suspend fun deletePost(post: CommunityPostEntity)

    // 如果未来需要，可以添加根据ID查询、删除等方法
    // @Query("SELECT * FROM community_posts WHERE id = :postId")
    // suspend fun getPostById(postId: Int): CommunityPostEntity?
    //
    // @Query("DELETE FROM community_posts WHERE id = :postId")
    // suspend fun deletePostById(postId: Int)
}
