package com.example.icyclist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.icyclist.adapter.CommentAdapter
import com.example.icyclist.database.CommentEntity
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.LikeEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.ActivityPostDetailBinding
import com.example.icyclist.manager.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 帖子详情页面
 * 显示帖子内容和评论列表
 */
class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var sportDatabase: SportDatabase
    private var post: CommunityPostEntity? = null
    private val comments = mutableListOf<CommentEntity>()
    private var commentAdapter: CommentAdapter? = null

    companion object {
        const val EXTRA_POST_ID = "post_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sportDatabase = SportDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()

        // 获取帖子ID并加载数据
        val postId = intent.getIntExtra(EXTRA_POST_ID, 0)
        if (postId > 0) {
            loadPostDetail(postId)
        } else {
            Toast.makeText(this, "帖子不存在", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(comments)
        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            adapter = commentAdapter
        }
    }

    private fun setupButtons() {
        // 点赞按钮
        binding.btnLike.setOnClickListener {
            post?.let { handleLike(it) }
        }

        // 发送评论按钮
        binding.btnSendComment.setOnClickListener {
            val content = binding.etCommentInput.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            post?.let { sendComment(it.id, content) }
        }
    }

    /**
     * 从服务器加载帖子详情（带本地缓存）
     */
    private fun loadPostDetail(postId: Int) {
        lifecycleScope.launch {
            try {
                // 从服务器获取帖子详情
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@PostDetailActivity)
                val response = apiService.getPostById(postId.toLong())

                if (response.isSuccessful && response.body() != null) {
                    val networkPost = response.body()!!
                    
                    // 转换为本地实体
                    post = CommunityPostEntity(
                        id = networkPost.id?.toInt() ?: postId,
                        userNickname = networkPost.authorName,
                        userAvatar = networkPost.authorAvatar,
                        content = networkPost.content ?: "",
                        imageUrl = networkPost.imageUrls?.firstOrNull(),
                        timestamp = System.currentTimeMillis(),
                        likes = networkPost.likeCount,
                        comments = networkPost.commentCount
                    )
                    
                    // 显示帖子内容
                    displayPost(post!!)
                    
                    // 加载点赞和评论状态
                    loadLikeState(post!!.id)
                    loadComments(post!!.id)
                    
                    android.util.Log.d("PostDetailActivity", "✅ 从服务器加载帖子详情")
                } else {
                    // 服务器请求失败，从本地缓存加载
                    loadFromLocalCache(postId)
                }
            } catch (e: Exception) {
                // 网络错误，从本地缓存加载
                android.util.Log.e("PostDetailActivity", "网络错误: ${e.message}", e)
                loadFromLocalCache(postId)
            }
        }
    }
    
    /**
     * 从本地缓存加载（作为后备方案）
     */
    private fun loadFromLocalCache(postId: Int) {
        lifecycleScope.launch {
            try {
                post = withContext(Dispatchers.IO) {
                    sportDatabase.communityPostDao().getAllPosts()
                        .find { it.id == postId }
                }

                post?.let { currentPost ->
                    displayPost(currentPost)
                    loadLikeState(currentPost.id)
                    loadComments(currentPost.id)
                } ?: run {
                    Toast.makeText(this@PostDetailActivity, "帖子不存在", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostDetailActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * 显示帖子内容
     */
    private fun displayPost(post: CommunityPostEntity) {
        binding.tvPostNickname.text = post.userNickname
        binding.tvPostContent.text = post.content
        binding.tvPostTime.text = formatTimestamp(post.timestamp)

        // 加载用户头像
        val avatarResourceId = resources.getIdentifier(
            post.userAvatar, "drawable", packageName
        )
        if (avatarResourceId != 0) {
            Glide.with(this)
                .load(avatarResourceId)
                .circleCrop()
                .into(binding.ivPostAvatar)
        }

        // 加载帖子图片
        if (!post.imageUrl.isNullOrEmpty()) {
            binding.ivPostImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(post.imageUrl)
                .into(binding.ivPostImage)
        } else {
            binding.ivPostImage.visibility = View.GONE
        }
    }

    /**
     * 加载点赞状态
     */
    private fun loadLikeState(postId: Int) {
        lifecycleScope.launch {
            val currentUserId = UserManager.getCurrentUserEmail(this@PostDetailActivity) ?: ""

            val likeCount = withContext(Dispatchers.IO) {
                sportDatabase.likeDao().getLikeCount(postId)
            }
            val isLiked = withContext(Dispatchers.IO) {
                sportDatabase.likeDao().isLiked(postId, currentUserId)
            }

            binding.tvLikeCount.text = likeCount.toString()
            if (isLiked) {
                binding.btnLike.setImageResource(R.drawable.ic_liked)
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_like)
            }
        }
    }

    /**
     * 加载评论列表（从服务器）
     */
    private fun loadComments(postId: Int) {
        lifecycleScope.launch {
            try {
                // 优先从服务器获取评论
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@PostDetailActivity)
                val response = apiService.getPostComments(postId.toLong())
                
                val commentList = if (response.isSuccessful && response.body() != null) {
                    val serverComments = response.body()!!
                    
                    // 转换为本地实体并保存到本地缓存
                    serverComments.map { serverComment ->
                        val commentEntity = CommentEntity(
                            id = serverComment.id?.toInt() ?: 0,
                            postId = postId,
                            userId = serverComment.userId.toString(),
                            userNickname = serverComment.user?.nickname ?: "骑行者",
                            userAvatar = serverComment.user?.avatar ?: "",
                            content = serverComment.content ?: "",
                            timestamp = System.currentTimeMillis()
                        )
                        
                        // 保存到本地
                        withContext(Dispatchers.IO) {
                            sportDatabase.commentDao().insertComment(commentEntity)
                        }
                        
                        commentEntity
                    }
                } else {
                    // 服务器请求失败，从本地缓存加载
                    withContext(Dispatchers.IO) {
                        sportDatabase.commentDao().getCommentsByPostId(postId)
                    }
                }

                comments.clear()
                comments.addAll(commentList)
                commentAdapter?.notifyDataSetChanged()

                // 更新评论数量显示
                binding.tvCommentCount.text = "${commentList.size} 条评论"

                // 显示/隐藏空状态提示
                if (commentList.isEmpty()) {
                    binding.tvEmptyComments.visibility = View.VISIBLE
                    binding.recyclerViewComments.visibility = View.GONE
                } else {
                    binding.tvEmptyComments.visibility = View.GONE
                    binding.recyclerViewComments.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                android.util.Log.e("PostDetailActivity", "加载评论失败: ${e.message}", e)
                // 异常时从本地缓存加载
                val commentList = withContext(Dispatchers.IO) {
                    sportDatabase.commentDao().getCommentsByPostId(postId)
                }
                
                comments.clear()
                comments.addAll(commentList)
                commentAdapter?.notifyDataSetChanged()
                
                binding.tvCommentCount.text = "${commentList.size} 条评论"
                
                if (commentList.isEmpty()) {
                    binding.tvEmptyComments.visibility = View.VISIBLE
                    binding.recyclerViewComments.visibility = View.GONE
                } else {
                    binding.tvEmptyComments.visibility = View.GONE
                    binding.recyclerViewComments.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 处理点赞/取消点赞 - 调用服务器API
     */
    private fun handleLike(post: CommunityPostEntity) {
        lifecycleScope.launch {
            try {
                // 调用服务器API
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@PostDetailActivity)
                val response = apiService.toggleLike(post.id.toLong())

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val isLiked = result["liked"] ?: false
                    
                    // 同时更新本地缓存
                    val currentUserId = UserManager.getCurrentUserEmail(this@PostDetailActivity) ?: ""
                    withContext(Dispatchers.IO) {
                        if (isLiked) {
                            val like = LikeEntity(postId = post.id, userId = currentUserId)
                            sportDatabase.likeDao().insertLike(like)
                        } else {
                            sportDatabase.likeDao().deleteLike(post.id, currentUserId)
                        }
                    }
                    
                    // 更新UI
                    loadLikeState(post.id)
                    
                    android.util.Log.d("PostDetailActivity", "点赞操作成功: $isLiked")
                } else {
                    Toast.makeText(this@PostDetailActivity, "操作失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("PostDetailActivity", "点赞失败", e)
                Toast.makeText(this@PostDetailActivity, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 发送评论 - 调用服务器API
     */
    private fun sendComment(postId: Int, content: String) {
        lifecycleScope.launch {
            try {
                // 创建评论请求对象
                val commentRequest = com.example.icyclist.network.model.Comment(
                    postId = postId.toLong(),
                    content = content
                )
                
                // 调用服务器API
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@PostDetailActivity)
                val response = apiService.addComment(postId.toLong(), commentRequest)

                if (response.isSuccessful && response.body() != null) {
                    val createdComment = response.body()!!
                    
                    // 同时保存到本地缓存
                    withContext(Dispatchers.IO) {
                        val commentEntity = CommentEntity(
                            id = createdComment.id?.toInt() ?: 0,
                            postId = postId,
                            userId = createdComment.userId.toString(),
                            userNickname = createdComment.user?.nickname ?: "骑行者",
                            userAvatar = createdComment.user?.avatar ?: "",
                            content = createdComment.content ?: "",
                            timestamp = System.currentTimeMillis()
                        )
                        sportDatabase.commentDao().insertComment(commentEntity)
                    }
                    
                    // 清空输入框
                    binding.etCommentInput.text.clear()
                    binding.etCommentInput.clearFocus()
                    
                    // 重新加载评论列表
                    loadComments(postId)
                    
                    Toast.makeText(this@PostDetailActivity, "评论成功", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("PostDetailActivity", "评论发送成功")
                } else {
                    Toast.makeText(this@PostDetailActivity, "评论失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("PostDetailActivity", "评论失败", e)
                Toast.makeText(this@PostDetailActivity, "评论失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}


