package com.example.icyclist.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Reply
import com.example.icyclist.adapter.ReplyAdapter
import com.example.icyclist.databinding.ActivityTopicDetailBinding
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.launch

class TopicDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicDetailBinding
    private var topicId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicTitle = intent.getStringExtra("TOPIC_TITLE") ?: "帖子详情"
        topicId = intent.getLongExtra("TOPIC_ID", 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = topicTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setupRepliesRecyclerView()
        
        // 如果有 topicId，从网络加载数据
        if (topicId > 0) {
            loadTopicDetails(topicId)
        } else {
            showFallbackData()
        }
    }

    private fun setupRepliesRecyclerView() {
        binding.repliesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * 从网络加载主题详情
     */
    private fun loadTopicDetails(topicId: Long) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@TopicDetailActivity)
                val response = apiService.getTopicDetails(topicId)
                
                if (response.isSuccessful) {
                    val topic = response.body()
                    
                    if (topic != null) {
                        // 更新主题信息
                        binding.topicTitle.text = topic.title
                        binding.authorName.text = topic.user?.nickname ?: "匿名用户"
                        binding.topicBody.text = topic.content
                        
                        // 更新回复列表
                        val replies = topic.replies?.map { networkReply ->
                            Reply(
                                id = networkReply.id?.toInt() ?: 0,
                                authorName = networkReply.user?.nickname ?: "匿名用户",
                                content = networkReply.content
                            )
                        } ?: emptyList()
                        
                        binding.repliesRecyclerView.adapter = ReplyAdapter(replies)
                    } else {
                        showErrorAndUseFallbackData("主题不存在")
                    }
                } else {
                    showErrorAndUseFallbackData("加载失败: ${response.code()}")
                }
            } catch (e: Exception) {
                showErrorAndUseFallbackData("网络错误: ${e.message}")
            }
        }
    }

    /**
     * 显示错误信息并使用后备数据
     */
    private fun showErrorAndUseFallbackData(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        showFallbackData()
    }

    /**
     * 显示后备假数据
     */
    private fun showFallbackData() {
        binding.topicTitle.text = intent.getStringExtra("TOPIC_TITLE") ?: "帖子详情"
        binding.authorName.text = "骑行小白"
        binding.topicBody.text = "上周刚入手一辆新公路车，骑了两次之后发现每次用力踩踏的时候，链条都会传来咔咔的异响，请问各位大佬这是什么原因？是需要调整变速器还是链条需要上油了？"
        
        val sampleReplies = listOf(
            Reply(1, "资深技师", "大概率是新链条和飞轮没有磨合好，建议先骑50-100公里看看。如果还响，检查一下后拨限位螺丝。"),
            Reply(2, "骑行小白", "好的，感谢大佬！我周末再去骑骑看。"),
            Reply(3, "热心车友", "也可以检查一下脚踏是不是松了，有时候异响来源很奇怪的。")
        )
        
        binding.repliesRecyclerView.adapter = ReplyAdapter(sampleReplies)
    }
}
