package com.example.icyclist.community

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.icyclist.database.ForumTopicEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.ActivityCreateTopicBinding
import com.example.icyclist.manager.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 创建论坛主题Activity
 */
class CreateTopicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTopicBinding
    private lateinit var sportDatabase: SportDatabase
    private var categoryId: Int = 0
    private val maxWordCount = 500

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_NAME = "category_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sportDatabase = SportDatabase.getDatabase(this)

        // 获取分类信息
        categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, 0)
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "论坛"

        if (categoryId == 0) {
            Toast.makeText(this, "分类信息错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar(categoryName)
        setupTextWatcher()
        setupButtons()
    }

    private fun setupToolbar(categoryName: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "发布主题 - $categoryName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }
    }

    private fun setupTextWatcher() {
        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWordCount(s?.length ?: 0)
            }
        })
    }

    private fun updateWordCount(count: Int) {
        binding.tvWordCount.text = "$count/$maxWordCount"
        if (count > maxWordCount) {
            binding.tvWordCount.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            binding.tvWordCount.setTextColor(getColor(android.R.color.darker_gray))
        }
    }

    private fun setupButtons() {
        binding.btnPublish.setOnClickListener {
            publishTopic()
        }
    }

    private fun publishTopic() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        // 验证输入
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            binding.etTitle.requestFocus()
            return
        }

        if (title.length > 100) {
            Toast.makeText(this, "标题不能超过100字", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.length > maxWordCount) {
            Toast.makeText(this, "内容不能超过$maxWordCount 字", Toast.LENGTH_SHORT).show()
            return
        }

        // 禁用按钮，防止重复点击
        binding.btnPublish.isEnabled = false
        binding.btnPublish.text = "发布中..."

        lifecycleScope.launch {
            try {
                // 创建网络请求对象
                val topicRequest = com.example.icyclist.network.model.forum.ForumTopic(
                    categoryId = categoryId.toLong(),
                    title = title,
                    content = content
                )
                
                // 提交到服务器
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@CreateTopicActivity)
                val response = apiService.createTopic(topicRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val createdTopic = response.body()!!
                    
                    // 同时保存到本地缓存
                    withContext(Dispatchers.IO) {
                        val topicEntity = ForumTopicEntity(
                            id = createdTopic.id?.toInt() ?: 0,
                            categoryId = categoryId,
                            userId = createdTopic.userId.toString(),
                            userNickname = createdTopic.authorName,
                            userAvatar = createdTopic.authorAvatar,
                            title = createdTopic.title,
                            content = createdTopic.content,
                            timestamp = System.currentTimeMillis(),
                            replyCount = 0
                        )
                        sportDatabase.forumTopicDao().insertTopic(topicEntity)
                        sportDatabase.forumCategoryDao().incrementTopicCount(categoryId)
                    }
                    
                    Toast.makeText(this@CreateTopicActivity, "发布成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateTopicActivity, "发布失败: ${response.code()}", Toast.LENGTH_LONG).show()
                    binding.btnPublish.isEnabled = true
                    binding.btnPublish.text = "发布"
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateTopicActivity", "发布失败", e)
                Toast.makeText(this@CreateTopicActivity, "发布失败: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "发布"
            }
        }
    }

    private fun handleBackPressed() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isNotEmpty() || content.isNotEmpty()) {
            showExitConfirmationDialog()
        } else {
            finish()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("放弃发布")
            .setMessage("您有未发布的内容，确定要放弃吗？")
            .setPositiveButton("放弃") { _, _ ->
                finish()
            }
            .setNegativeButton("继续编辑", null)
            .show()
    }

    override fun onBackPressed() {
        handleBackPressed()
    }
}


