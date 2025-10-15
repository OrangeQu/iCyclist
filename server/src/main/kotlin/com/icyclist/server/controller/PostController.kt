package com.icyclist.server.controller

import com.icyclist.server.model.Comment
import com.icyclist.server.model.Post
import com.icyclist.server.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(private val postService: PostService) {

    @PostMapping
    fun createPost(@AuthenticationPrincipal userId: Long, @RequestBody post: Post): ResponseEntity<Post> {
        val createdPost = postService.createPost(post, userId)
        return ResponseEntity.ok(createdPost)
    }

    @GetMapping
    fun getTimeline(): ResponseEntity<List<Post>> {
        val posts = postService.getTimelinePosts()
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/{id}")
    fun getPostById(@PathVariable id: Long): ResponseEntity<Post> {
        val post = postService.getPostById(id)
        return post?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/{id}/comments")
    fun addComment(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody comment: Comment
    ): ResponseEntity<Comment> {
        comment.postId = id
        val createdComment = postService.createComment(comment, userId)
        return ResponseEntity.ok(createdComment)
    }

    @PostMapping("/{id}/like")
    fun toggleLike(@PathVariable id: Long, @AuthenticationPrincipal userId: Long): ResponseEntity<Map<String, Boolean>> {
        val isLiked = postService.toggleLike(id, userId)
        return ResponseEntity.ok(mapOf("liked" to isLiked))
    }
}





