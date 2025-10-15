package com.icyclist.server.mapper

import com.icyclist.server.model.Comment
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommentMapper {
    fun insert(comment: Comment)
    fun findByPostId(postId: Long): List<Comment>
}





