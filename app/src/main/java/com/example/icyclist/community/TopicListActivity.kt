package com.example.icyclist.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Topic
import com.example.icyclist.adapter.TopicAdapter
import com.example.icyclist.databinding.ActivityTopicListBinding

class TopicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "论坛"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = categoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // TODO: Replace with actual data from your backend
        val sampleTopics = listOf(
            Topic(1, "求助！我的新车链条异响怎么办？", "骑行小白", 32),
            Topic(2, "【干货】长途骑行装备清单，助你轻松远行", "旅行的骑士", 128),
            Topic(3, "周末休闲骑路线推荐 (50km以内)", "城市探险家", 76),
            Topic(4, "关于锁鞋的选择，大家有什么建议吗？", "装备党", 54),
            Topic(5, "晒一晒你的爱车！", "摄影师小张", 210)
        )

        binding.topicsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.topicsRecyclerView.adapter = TopicAdapter(sampleTopics)
    }
}
