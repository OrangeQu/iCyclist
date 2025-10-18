package com.icyclist.server.service

import com.icyclist.server.mapper.CommentMapper
import com.icyclist.server.mapper.PostLikeMapper
import com.icyclist.server.mapper.PostMapper
import com.icyclist.server.model.Comment
import com.icyclist.server.model.Post
import com.icyclist.server.model.PostLike
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postMapper: PostMapper,
    private val commentMapper: CommentMapper,
    private val postLikeMapper: PostLikeMapper
) {

    fun createPost(post: Post, userId: Long): Post {
        post.userId = userId
        postMapper.insert(post)
        // Refetch to get user data populated from the join query
        return postMapper.findById(post.id!!)!!
    }

    fun getTimelinePosts(): List<Post> {
        val posts = postMapper.findAll()
        posts.forEach { post ->
            post.comments = commentMapper.findByPostId(post.id!!)
            post.likes = postLikeMapper.findByPostId(post.id!!)
        }
        return posts
    }

    fun getPostById(id: Long): Post? {
        val post = postMapper.findById(id)
        post?.let {
            it.comments = commentMapper.findByPostId(it.id!!)
            it.likes = postLikeMapper.findByPostId(it.id!!)
        }
        return post
    }

    fun getPostComments(postId: Long): List<Comment> {
        return commentMapper.findByPostId(postId)
    }

    fun createComment(comment: Comment, userId: Long): Comment {
        comment.userId = userId
        commentMapper.insert(comment)
        // Refetch to get user data populated
        return commentMapper.findByPostId(comment.postId).lastOrNull() ?: comment
    }

    fun getPostLikes(postId: Long): List<PostLike> {
        return postLikeMapper.findByPostId(postId)
    }

    fun toggleLike(postId: Long, userId: Long): Boolean {
        return if (postLikeMapper.exists(postId, userId)) {
            postLikeMapper.delete(postId, userId)
            false // Liked -> Unliked
        } else {
            postLikeMapper.insert(PostLike(postId = postId, userId = userId))
            true // Unliked -> Liked
        }
    }
}




















