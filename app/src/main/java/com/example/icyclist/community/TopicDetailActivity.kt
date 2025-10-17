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
        // 如果没有，可以在点击时显示一个对话框
        // 这里先用一个简单的Toast提示，后续可以优化为对话框
        binding.root.findViewById<View?>(com.example.icyclist.R.id.btn_send_reply)?.setOnClickListener {
            sendReply()
        }
    }

    /**
     * 从本地数据库加载主题详情
     */
    private fun loadTopicFromDatabase(topicId: Int) {
        lifecycleScope.launch {
            try {
                // 加载主题信息
                val topic = withContext(Dispatchers.IO) {
                    sportDatabase.forumTopicDao().getTopicById(topicId)
                }
                
                if (topic != null) {
                    // 更新主题信息
                    binding.topicTitle.text = topic.title
                    binding.authorName.text = topic.userNickname
                    binding.topicBody.text = topic.content
                    
                    // 加载回复列表
                    loadReplies(topicId)
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
     * 加载回复列表
     */
    private fun loadReplies(topicId: Int) {
        lifecycleScope.launch {
            try {
                val dbReplies = withContext(Dispatchers.IO) {
                    sportDatabase.forumReplyDao().getRepliesByTopicId(topicId)
                }
                
                // 转换为Adapter需要的格式
                replies.clear()
                replies.addAll(dbReplies.map { dbReply ->
                    Reply(
                        id = dbReply.id,
                        authorName = dbReply.userNickname,
                        content = dbReply.content
                    )
                })
                
                replyAdapter?.notifyDataSetChanged()
                
            } catch (e: Exception) {
                Toast.makeText(this@TopicDetailActivity, "加载回复失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 发送回复
     * 由于布局可能没有输入框，这里使用AlertDialog
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
                        
                        // 重新加载回复列表
                        loadReplies(topicId)
                        
                    } catch (e: Exception) {
                        Toast.makeText(this@TopicDetailActivity, "回复失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
