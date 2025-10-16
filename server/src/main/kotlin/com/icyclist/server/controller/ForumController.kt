package com.icyclist.server.controller

import com.icyclist.server.model.ForumReply
import com.icyclist.server.model.ForumTopic
import com.icyclist.server.service.ForumService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/forum")
class ForumController(private val forumService: ForumService) {

    @GetMapping("/categories")
    fun getAllCategories() = ResponseEntity.ok(forumService.getAllCategories())

    @GetMapping("/categories/{categoryId}/topics")
    fun getTopicsByCategory(@PathVariable categoryId: Long) =
        ResponseEntity.ok(forumService.getTopicsByCategoryId(categoryId))

    @PostMapping("/topics")
    fun createTopic(@AuthenticationPrincipal userId: Long, @RequestBody topic: ForumTopic): ResponseEntity<ForumTopic> {
        val createdTopic = forumService.createTopic(topic, userId)
        return ResponseEntity.ok(createdTopic)
    }

    @GetMapping("/topics/{topicId}")
    fun getTopicDetails(@PathVariable topicId: Long): ResponseEntity<ForumTopic> {
        val topic = forumService.getTopicDetails(topicId)
        return topic?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/topics/{topicId}/replies")
    fun createReply(
        @PathVariable topicId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody reply: ForumReply
    ): ResponseEntity<ForumReply> {
        reply.topicId = topicId
        val createdReply = forumService.createReply(reply, userId)
        return ResponseEntity.ok(createdReply)
    }
}














