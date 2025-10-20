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

}
