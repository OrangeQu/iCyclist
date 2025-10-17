package com.icyclist.server.mapper

import com.icyclist.server.model.ForumCategory
import com.icyclist.server.model.ForumReply
import com.icyclist.server.model.ForumTopic
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ForumMapper {
    // Category
    fun findAllCategories(): List<ForumCategory>
    fun findCategoryById(id: Long): ForumCategory?

    // Topic
    fun insertTopic(topic: ForumTopic)
    fun findTopicsByCategoryId(categoryId: Long): List<ForumTopic>
    fun findTopicById(id: Long): ForumTopic?

    // Reply
    fun insertReply(reply: ForumReply)
    fun findRepliesByTopicId(topicId: Long): List<ForumReply>
}
















