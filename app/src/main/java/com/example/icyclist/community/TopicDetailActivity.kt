package com.example.icyclist.community

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Reply
import com.example.icyclist.adapter.ReplyAdapter
import com.example.icyclist.database.ForumReplyEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.ActivityTopicDetailBinding
import com.example.icyclist.manager.UserManager
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicDetailBinding
    private lateinit var sportDatabase: SportDatabase
    private var topicId: Int = 0
    private val replies = mutableListOf<Reply>()
    private var replyAdapter: ReplyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sportDatabase = SportDatabase.getDatabase(this)

        val topicTitle = intent.getStringExtra("TOPIC_TITLE") ?: "帖子详情"
        topicId = intent.getIntExtra("TOPIC_ID", 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = topicTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setupRepliesRecyclerView()
        setupReplyButton()
        
        // 从本地数据库加载数据
        if (topicId > 0) {
            loadTopicFromDatabase(topicId)
        } else {
            Toast.makeText(this, "主题不存在", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRepliesRecyclerView() {
        replyAdapter = ReplyAdapter(replies)
        binding.repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.repliesRecyclerView.adapter = replyAdapter
    }
    
    private fun setupReplyButton() {
        // 检查布局中是否有回复输入框和按钮
        binding.root.findViewById<View?>(com.example.icyclist.R.id.btn_send_reply)?.setOnClickListener {
            sendReply()
        }
    }

    /**
     * 从服务器加载主题详情（带本地缓存）
     */
    private fun loadTopicFromDatabase(topicId: Int) {
        lifecycleScope.launch {
            try {
                // 从服务器获取主题详情
                val apiService = RetrofitClient.getApiService(this@TopicDetailActivity)
                val response = apiService.getTopicDetails(topicId.toLong())
                
                if (response.isSuccessful && response.body() != null) {
                    val networkTopic = response.body()!!
                    
                    // 更新主题信息
                    binding.topicTitle.text = networkTopic.title
                    binding.authorName.text = networkTopic.authorName
                    binding.topicBody.text = networkTopic.content
                    
                    // 加载回复列表
                    replies.clear()
                    networkTopic.replies?.let { replyList ->
                        replies.addAll(replyList.map { netReply ->
                            Reply(
                                id = netReply.id?.toInt() ?: 0,
                                authorName = netReply.authorName,
                                content = netReply.content
                            )
                        })
                    }
                    replyAdapter?.notifyDataSetChanged()
                    
                    // 保存到本地缓存（包括主题和回复）
                    withContext(Dispatchers.IO) {
                        // 保存主题
                        val topicEntity = com.example.icyclist.database.ForumTopicEntity(
                            id = networkTopic.id?.toInt() ?: topicId,
                            categoryId = networkTopic.categoryId.toInt(),
                            userId = networkTopic.userId.toString(),
                            userNickname = networkTopic.authorName,
                            userAvatar = networkTopic.authorAvatar,
                            title = networkTopic.title,
                            content = networkTopic.content,
                            timestamp = System.currentTimeMillis(),
                            replyCount = networkTopic.replyCount
                        )
                        sportDatabase.forumTopicDao().insertTopic(topicEntity)
                        
                        // 保存回复到本地数据库
                        networkTopic.replies?.forEach { netReply ->
                            val replyEntity = ForumReplyEntity(
                                id = netReply.id?.toInt() ?: 0,
                                topicId = topicId,
                                userId = netReply.userId.toString(),
                                userNickname = netReply.authorName,
                                userAvatar = netReply.authorAvatar,
                                content = netReply.content,
                                timestamp = System.currentTimeMillis()
                            )
                            sportDatabase.forumReplyDao().insertReply(replyEntity)
                        }
                    }
                    
                    android.util.Log.d("TopicDetailActivity", "✅ 从服务器加载主题详情成功")
                } else {
                    // 服务器请求失败，从本地缓存加载
                    loadFromLocalCache(topicId)
                }
            } catch (e: Exception) {
                // 网络错误，从本地缓存加载
                android.util.Log.e("TopicDetailActivity", "网络错误: ${e.message}", e)
                loadFromLocalCache(topicId)
            }
        }
    }
    
    /**
     * 从本地缓存加载（作为后备方案）
     */
    private fun loadFromLocalCache(topicId: Int) {
        lifecycleScope.launch {
            try {
                val topic = withContext(Dispatchers.IO) {
                    sportDatabase.forumTopicDao().getTopicById(topicId)
                }
                
                if (topic != null) {
                    binding.topicTitle.text = topic.title
                    binding.authorName.text = topic.userNickname
                    binding.topicBody.text = topic.content
                    
                    loadRepliesFromCache(topicId)
                } else {
                    Toast.makeText(this@TopicDetailActivity, "主题不存在", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TopicDetailActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 从本地缓存加载回复列表
     */
    private fun loadRepliesFromCache(topicId: Int) {
        lifecycleScope.launch {
            try {
                val dbReplies = withContext(Dispatchers.IO) {
                    sportDatabase.forumReplyDao().getRepliesByTopicId(topicId)
                }
                
                replies.clear()
                replies.addAll(dbReplies.map { dbReply ->
                    Reply(
                        id = dbReply.id,
                        authorName = dbReply.userNickname,
                        content = dbReply.content
                    )
                })
                
                replyAdapter?.notifyDataSetChanged()
                android.util.Log.d("TopicDetailActivity", "从本地缓存加载 ${replies.size} 条回复")
            } catch (e: Exception) {
                Toast.makeText(this@TopicDetailActivity, "加载回复失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 发送回复
     */
    private fun sendReply() {
        val input = android.widget.EditText(this)
        input.hint = "输入你的回复..."
        input.maxLines = 5
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("回复")
            .setView(input)
            .setPositiveButton("发送") { _, _ ->
                val content = input.text.toString().trim()
                if (content.isEmpty()) {
                    Toast.makeText(this, "请输入回复内容", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                lifecycleScope.launch {
                    try {
                        val currentUserId = UserManager.getCurrentUserEmail(this@TopicDetailActivity) ?: ""
                        val currentUserNickname = UserManager.getCurrentUserNickname(this@TopicDetailActivity) ?: "骑行者"
                        val currentUserAvatar = UserManager.getCurrentUserAvatar(this@TopicDetailActivity) ?: "ic_twotone_person_24"
                        
                        // 先尝试发送到服务器
                        var success = false
                        try {
                            val apiService = RetrofitClient.getApiService(this@TopicDetailActivity)
                            val userId = UserManager.getUserId(this@TopicDetailActivity) ?: 0L
                            val networkReply = com.example.icyclist.network.model.forum.ForumReply(
                                topicId = topicId.toLong(),
                                userId = userId,
                                content = content
                            )
                            val response = apiService.createReply(topicId.toLong(), networkReply)
                            
                            if (response.isSuccessful && response.body() != null) {
                                success = true
                                android.util.Log.d("TopicDetailActivity", "✅ 回复已发送到服务器")
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("TopicDetailActivity", "发送到服务器失败，仅保存到本地: ${e.message}")
                        }
                        
                        // 保存到本地数据库（作为缓存或备用）
                        val reply = ForumReplyEntity(
                            topicId = topicId,
                            userId = currentUserId,
                            userNickname = currentUserNickname,
                            userAvatar = currentUserAvatar,
                            content = content
                        )
                        
                        withContext(Dispatchers.IO) {
                            sportDatabase.forumReplyDao().insertReply(reply)
                            sportDatabase.forumTopicDao().incrementReplyCount(topicId)
                        }
                        
                        Toast.makeText(this@TopicDetailActivity, "回复成功", Toast.LENGTH_SHORT).show()
                        
                        // 重新加载主题详情（包括回复）
                        loadTopicFromDatabase(topicId)
                        
                    } catch (e: Exception) {
                        android.util.Log.e("TopicDetailActivity", "回复失败", e)
                        Toast.makeText(this@TopicDetailActivity, "回复失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
