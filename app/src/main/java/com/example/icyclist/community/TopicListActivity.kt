package com.example.icyclist.community

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Topic
import com.example.icyclist.adapter.TopicAdapter
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.ActivityTopicListBinding
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicListBinding
    private lateinit var sportDatabase: SportDatabase
    private var categoryId: Int = 0
    private var categoryName: String = "论坛"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sportDatabase = SportDatabase.getDatabase(this)

        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "论坛"
        categoryId = intent.getIntExtra("CATEGORY_ID", 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = categoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRecyclerView()
        setupFab()
        
        // 从本地数据库加载数据
        if (categoryId > 0) {
            loadTopicsFromDatabase(categoryId)
        } else {
            Toast.makeText(this, "分类信息错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到这个界面时刷新数据
        if (categoryId > 0) {
            loadTopicsFromDatabase(categoryId)
        }
    }

    private fun setupRecyclerView() {
        binding.topicsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupFab() {
        binding.fabCreateTopic.setOnClickListener {
            val intent = Intent(this, CreateTopicActivity::class.java)
            intent.putExtra(CreateTopicActivity.EXTRA_CATEGORY_ID, categoryId)
            intent.putExtra(CreateTopicActivity.EXTRA_CATEGORY_NAME, categoryName)
            startActivity(intent)
        }
    }

    /**
     * 从服务器加载主题列表（带本地缓存）
     */
    private fun loadTopicsFromDatabase(categoryId: Int) {
        lifecycleScope.launch {
            try {
                // 先从服务器获取数据
                val apiService = RetrofitClient.getApiService(this@TopicListActivity)
                val response = apiService.getTopicsByCategory(categoryId.toLong())
                
                if (response.isSuccessful && response.body() != null) {
                    val networkTopics = response.body()!!
                    
                    // 转换为 Adapter 需要的格式，并从本地数据库获取实际回复数量
                    val topics = mutableListOf<Topic>()
                    for (netTopic in networkTopics) {
                        val topicId = netTopic.id?.toInt() ?: 0
                        // 从本地数据库查询实际的回复数量
                        val actualReplyCount = withContext(Dispatchers.IO) {
                            sportDatabase.forumReplyDao().getReplyCount(topicId)
                        }
                        topics.add(Topic(
                            id = topicId,
                            title = netTopic.title,
                            authorName = netTopic.authorName ?: "匿名",
                            replyCount = actualReplyCount  // 使用实际查询到的数量
                        ))
                    }
                    
                    // 更新 RecyclerView
                    binding.topicsRecyclerView.adapter = TopicAdapter(topics)
                    
                    // 保存到本地缓存
                    withContext(Dispatchers.IO) {
                        networkTopics.forEach { netTopic ->
                            val entity = com.example.icyclist.database.ForumTopicEntity(
                                id = netTopic.id?.toInt() ?: 0,
                                categoryId = categoryId,
                                userId = netTopic.authorId.toString(),
                                userNickname = netTopic.authorName,
                                userAvatar = netTopic.authorAvatar,
                                title = netTopic.title,
                                content = netTopic.content,
                                timestamp = System.currentTimeMillis(),
                                replyCount = netTopic.replyCount
                            )
                            sportDatabase.forumTopicDao().insertTopic(entity)
                        }
                    }
                    
                    android.util.Log.d("TopicListActivity", "✅ 从服务器加载 ${topics.size} 个主题")
                } else {
                    // 服务器请求失败，从本地缓存加载
                    loadFromLocalCache(categoryId)
                }
            } catch (e: Exception) {
                // 网络错误，从本地缓存加载
                android.util.Log.e("TopicListActivity", "网络错误: ${e.message}", e)
                loadFromLocalCache(categoryId)
            }
        }
    }
    
    /**
     * 从本地缓存加载（作为后备方案）
     */
    private fun loadFromLocalCache(categoryId: Int) {
        lifecycleScope.launch {
            try {
                val dbTopics = withContext(Dispatchers.IO) {
                    sportDatabase.forumTopicDao().getTopicsByCategory(categoryId)
                }
                
                // 为每个主题实时查询回复数量
                val topics = mutableListOf<Topic>()
                for (dbTopic in dbTopics) {
                    val actualReplyCount = withContext(Dispatchers.IO) {
                        sportDatabase.forumReplyDao().getReplyCount(dbTopic.id)
                    }
                    topics.add(Topic(
                        id = dbTopic.id,
                        title = dbTopic.title,
                        authorName = dbTopic.userNickname,
                        replyCount = actualReplyCount  // 使用实时查询到的数量
                    ))
                }
                
                binding.topicsRecyclerView.adapter = TopicAdapter(topics)
                android.util.Log.d("TopicListActivity", "从本地缓存加载 ${topics.size} 个主题")
            } catch (e: Exception) {
                Toast.makeText(this@TopicListActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
