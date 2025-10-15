package com.example.icyclist.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.Reply
import com.example.icyclist.adapter.ReplyAdapter
import com.example.icyclist.databinding.ActivityTopicDetailBinding

class TopicDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicTitle = intent.getStringExtra("TOPIC_TITLE") ?: "帖子详情"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = topicTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setupViews()
        setupRepliesRecyclerView()
    }

    private fun setupViews() {
        // TODO: Replace with actual data passed from the intent
        binding.topicTitle.text = intent.getStringExtra("TOPIC_TITLE") ?: "帖子详情"
        binding.authorName.text = "骑行小白"
        binding.topicBody.text = "上周刚入手一辆新公路车，骑了两次之后发现每次用力踩踏的时候，链条都会传来“咔咔”的异响，请问各位大佬这是什么原因？是需要调整变速器还是链条需要上油了？"
    }

    private fun setupRepliesRecyclerView() {
        // TODO: Replace with actual reply data for the topic
        val sampleReplies = listOf(
            Reply(1, "资深技师", "大概率是新链条和飞轮没有磨合好，建议先骑50-100公里看看。如果还响，检查一下后拨限位螺丝。"),
            Reply(2, "骑行小白", "好的，感谢大佬！我周末再去骑骑看。"),
            Reply(3, "热心车友", "也可以检查一下脚踏是不是松了，有时候异响来源很奇怪的。")
        )

        binding.repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.repliesRecyclerView.adapter = ReplyAdapter(sampleReplies)
    }
}
