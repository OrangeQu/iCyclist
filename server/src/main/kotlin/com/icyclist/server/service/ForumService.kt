package com.icyclist.server.service

import com.icyclist.server.mapper.ForumMapper
import com.icyclist.server.model.ForumCategory
import com.icyclist.server.model.ForumReply
import com.icyclist.server.model.ForumTopic
import org.springframework.stereotype.Service

@Service
class ForumService(private val forumMapper: ForumMapper) {

    fun getAllCategories(): List<ForumCategory> {
        return forumMapper.findAllCategories()
    }

    fun getTopicsByCategoryId(categoryId: Long): List<ForumTopic> {
        return forumMapper.findTopicsByCategoryId(categoryId)
    }

    fun getTopicDetails(topicId: Long): ForumTopic? {
        val topic = forumMapper.findTopicById(topicId)
        topic?.let {
            it.replies = forumMapper.findRepliesByTopicId(topicId)
        }
        return topic
    }

    fun createTopic(topic: ForumTopic, userId: Long): ForumTopic {
        topic.userId = userId
        forumMapper.insertTopic(topic)
        // Refetch to get user data populated
        return forumMapper.findTopicById(topic.id!!)!!
    }

    fun createReply(reply: ForumReply, userId: Long): ForumReply {
        reply.userId = userId
        forumMapper.insertReply(reply)
        // For simplicity, we don't refetch the full reply with user data here.
        // Can be enhanced if needed by adding a findReplyById method.
        return reply
    }
}

