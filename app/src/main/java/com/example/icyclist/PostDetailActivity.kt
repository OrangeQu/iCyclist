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
     * 加载帖子详情
     */
    private fun loadPostDetail(postId: Int) {
        lifecycleScope.launch {
            try {
                // 加载帖子信息
                post = withContext(Dispatchers.IO) {
                    sportDatabase.communityPostDao().getAllPosts()
                        .find { it.id == postId }
                }

                post?.let { currentPost ->
                    // 显示帖子内容
                    displayPost(currentPost)

                    // 加载点赞状态
                    loadLikeState(currentPost.id)

                    // 加载评论列表
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
     * 加载评论列表
     */
    private fun loadComments(postId: Int) {
        lifecycleScope.launch {
            val commentList = withContext(Dispatchers.IO) {
                sportDatabase.commentDao().getCommentsByPostId(postId)
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
        }
    }

    /**
     * 处理点赞/取消点赞
     */
    private fun handleLike(post: CommunityPostEntity) {
        lifecycleScope.launch {
            try {
                val currentUserId = UserManager.getCurrentUserEmail(this@PostDetailActivity) ?: ""

                val isLiked = withContext(Dispatchers.IO) {
                    sportDatabase.likeDao().isLiked(post.id, currentUserId)
                }

                if (isLiked) {
                    // 取消点赞
                    withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().deleteLike(post.id, currentUserId)
                    }
                } else {
                    // 点赞
                    val like = LikeEntity(
                        postId = post.id,
                        userId = currentUserId
                    )
                    withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().insertLike(like)
                    }
                }

                // 更新UI
                loadLikeState(post.id)

            } catch (e: Exception) {
                Toast.makeText(this@PostDetailActivity, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 发送评论
     */
    private fun sendComment(postId: Int, content: String) {
        lifecycleScope.launch {
            try {
                val currentUserId = UserManager.getCurrentUserEmail(this@PostDetailActivity) ?: ""
                val currentUserNickname = UserManager.getCurrentUserNickname(this@PostDetailActivity) ?: "骑行者"
                val currentUserAvatar = UserManager.getCurrentUserAvatar(this@PostDetailActivity) ?: "ic_twotone_person_24"

                val comment = CommentEntity(
                    postId = postId,
                    userId = currentUserId,
                    userNickname = currentUserNickname,
                    userAvatar = currentUserAvatar,
                    content = content
                )

                withContext(Dispatchers.IO) {
                    sportDatabase.commentDao().insertComment(comment)
                }

                // 清空输入框
                binding.etCommentInput.text.clear()

                // 隐藏键盘
                binding.etCommentInput.clearFocus()

                // 重新加载评论列表
                loadComments(postId)

                Toast.makeText(this@PostDetailActivity, "评论成功", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@PostDetailActivity, "评论失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}


