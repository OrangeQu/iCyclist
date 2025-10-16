package com.example.icyclist.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Topic
import com.example.icyclist.adapter.TopicAdapter
import com.example.icyclist.databinding.ActivityTopicListBinding
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.launch

class TopicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicListBinding
    private var categoryId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "论坛"
        categoryId = intent.getLongExtra("CATEGORY_ID", 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = categoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRecyclerView()
        
        // 如果有 categoryId，从网络加载数据
        if (categoryId > 0) {
            loadTopicsByCategory(categoryId)
        } else {
            showFallbackData()
        }
    }

    private fun setupRecyclerView() {
        binding.topicsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * 从网络加载主题列表
     */
    private fun loadTopicsByCategory(categoryId: Long) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@TopicListActivity)
                val response = apiService.getTopicsByCategory(categoryId)
                
                if (response.isSuccessful) {
                    val networkTopics = response.body() ?: emptyList()
                    
                    // 将网络数据转换为 Adapter 需要的格式
                    val topics = networkTopics.map { networkTopic ->
                        Topic(
                            id = networkTopic.id?.toInt() ?: 0,
                            title = networkTopic.title,
                            authorName = networkTopic.user?.nickname ?: "匿名用户",
                            replyCount = networkTopic.replies?.size ?: 0
                        )
                    }
                    
                    // 更新 RecyclerView
                    binding.topicsRecyclerView.adapter = TopicAdapter(topics)
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
        val sampleTopics = listOf(
            Topic(1, "求助！我的新车链条异响怎么办？", "骑行小白", 32),
            Topic(2, "【干货】长途骑行装备清单，助你轻松远行", "旅行的骑士", 128),
            Topic(3, "周末休闲骑路线推荐 (50km以内)", "城市探险家", 76),
            Topic(4, "关于锁鞋的选择，大家有什么建议吗？", "装备党", 54),
            Topic(5, "晒一晒你的爱车！", "摄影师小张", 210)
        )
        
        binding.topicsRecyclerView.adapter = TopicAdapter(sampleTopics)
    }
}
