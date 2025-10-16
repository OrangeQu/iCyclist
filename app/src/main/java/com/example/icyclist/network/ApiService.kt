package com.example.icyclist.network

import com.example.icyclist.network.model.Comment
import com.example.icyclist.network.model.Post
import com.example.icyclist.network.model.forum.ForumCategory
import com.example.icyclist.network.model.forum.ForumReply
import com.example.icyclist.network.model.forum.ForumTopic
import retrofit2.Response
import retrofit2.http.*

/**
 * iCyclist API 服务接口
 * 定义所有后端 API 接口
 */
interface ApiService {

    // ==================== 论坛相关接口 ====================
    
    /**
     * 获取所有论坛分类
     * GET /api/forum/categories
     */
    @GET("api/forum/categories")
    suspend fun getForumCategories(): Response<List<ForumCategory>>

    /**
     * 根据分类 ID 获取主题列表
     * GET /api/forum/categories/{categoryId}/topics
     */
    @GET("api/forum/categories/{categoryId}/topics")
    suspend fun getTopicsByCategory(@Path("categoryId") categoryId: Long): Response<List<ForumTopic>>

    /**
     * 创建新主题
     * POST /api/forum/topics
     */
    @POST("api/forum/topics")
    suspend fun createTopic(@Body topic: ForumTopic): Response<ForumTopic>

    /**
     * 获取主题详情（包含回复列表）
     * GET /api/forum/topics/{topicId}
     */
    @GET("api/forum/topics/{topicId}")
    suspend fun getTopicDetails(@Path("topicId") topicId: Long): Response<ForumTopic>

    /**
     * 创建主题回复
     * POST /api/forum/topics/{topicId}/replies
     */
    @POST("api/forum/topics/{topicId}/replies")
    suspend fun createReply(
        @Path("topicId") topicId: Long,
        @Body reply: ForumReply
    ): Response<ForumReply>

    // ==================== 骑行圈（帖子）相关接口 ====================

    /**
     * 创建帖子
     * POST /api/posts
     */
    @POST("api/posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    /**
     * 获取时间线帖子列表（骑行圈首页）
     * GET /api/posts
     */
    @GET("api/posts")
    suspend fun getTimelinePosts(): Response<List<Post>>

    /**
     * 根据 ID 获取帖子详情
     * GET /api/posts/{id}
     */
    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    /**
     * 给帖子添加评论
     * POST /api/posts/{id}/comments
     */
    @POST("api/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: Long,
        @Body comment: Comment
    ): Response<Comment>

    /**
     * 切换帖子点赞状态
     * POST /api/posts/{id}/like
     */
    @POST("api/posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: Long): Response<Map<String, Boolean>>

    // ==================== 用户认证相关接口 ====================
    // 注：如果后端有单独的用户登录注册接口，在此添加
    // 例如：
    // @POST("api/auth/login")
    // suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    // 
    // @POST("api/auth/register")
    // suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
}

