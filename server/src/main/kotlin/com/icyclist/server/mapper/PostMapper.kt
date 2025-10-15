package com.icyclist.server.mapper

import com.icyclist.server.model.Post
import org.apache.ibatis.annotations.Mapper

@Mapper
interface PostMapper {
    fun insert(post: Post)
    fun findById(id: Long): Post?
    fun findAll(): List<Post> // This will be a complex query
    fun findByUserId(userId: Long): List<Post>
}





