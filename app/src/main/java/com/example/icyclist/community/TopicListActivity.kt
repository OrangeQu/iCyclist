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
     * 从本地数据库加载主题列表
     */
    private fun loadTopicsFromDatabase(categoryId: Int) {
        lifecycleScope.launch {
            try {
                val dbTopics = withContext(Dispatchers.IO) {
                    sportDatabase.forumTopicDao().getTopicsByCategory(categoryId)
                }
                
                // 将数据库数据转换为 Adapter 需要的格式
                val topics = dbTopics.map { dbTopic ->
                    Topic(
                        id = dbTopic.id,
                        title = dbTopic.title,
                        authorName = dbTopic.userNickname,
                        replyCount = dbTopic.replyCount
                    )
                }
                
                // 更新 RecyclerView
                binding.topicsRecyclerView.adapter = TopicAdapter(topics)
                
            } catch (e: Exception) {
                Toast.makeText(this@TopicListActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
