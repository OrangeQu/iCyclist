package com.icyclist.server.mapper

import com.icyclist.server.model.PostLike
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface PostLikeMapper {
    fun insert(postLike: PostLike)
    fun delete(@Param("postId") postId: Long, @Param("userId") userId: Long)
    fun findByPostId(postId: Long): List<PostLike>
    fun exists(@Param("postId") postId: Long, @Param("userId") userId: Long): Boolean
}





