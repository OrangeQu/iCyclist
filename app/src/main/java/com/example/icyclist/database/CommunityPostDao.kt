package com.example.icyclist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    suspend fun getAllPosts(): List<CommunityPostEntity>
    
    @Query("SELECT * FROM community_posts WHERE id = :postId")
    suspend fun getPostById(postId: Long): CommunityPostEntity?
    
    @Insert
    suspend fun insertPost(post: CommunityPostEntity): Long
    
    @Delete
    suspend fun deletePost(post: CommunityPostEntity)
    
    @Query("DELETE FROM community_posts WHERE id = :postId")
    suspend fun deletePostById(postId: Long)
}
