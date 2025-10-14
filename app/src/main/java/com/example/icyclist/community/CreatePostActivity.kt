package com.example.icyclist.community

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.icyclist.databinding.ActivityCreatePostBinding
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.manager.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUri: Uri? = null
    private val maxWordCount = 500
    private lateinit var sportDatabase: SportDatabase
    private var savedImagePath: String? = null

    // 注册获取图片的结果
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showSelectedImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sportDatabase = SportDatabase.getDatabase(this)

        setupToolbar()
        setupViews()
        setupTextWatcher()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 添加返回按钮
        binding.toolbar.setNavigationIcon(com.example.icyclist.R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViews() {
        // 发布按钮点击事件
        binding.btnPublish.setOnClickListener {
            publishPost()
        }

        // 添加图片按钮点击事件
        binding.btnAddImage.setOnClickListener {
            selectImage()
        }

        // 移除图片按钮点击事件
        binding.btnRemoveImage.setOnClickListener {
            removeSelectedImage()
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

        // 根据字数改变颜色
        if (count > maxWordCount) {
            binding.tvWordCount.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            binding.tvWordCount.setTextColor(getColor(android.R.color.darker_gray))
        }
    }

    private fun selectImage() {
        // 打开图片选择器
        getContent.launch("image/*")
    }

    private fun showSelectedImage(uri: Uri) {
        binding.ivSelectedImage.setImageURI(uri)
        binding.layoutImagePreview.visibility = LinearLayout.VISIBLE
        saveImageToInternalStorage(uri)
    }

    private fun removeSelectedImage() {
        selectedImageUri = null
        savedImagePath = null
        binding.layoutImagePreview.visibility = LinearLayout.GONE
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val imagesDir = File(filesDir, "post_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val imageFile = File(imagesDir, "post_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(imageFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            savedImagePath = imageFile.absolutePath
        } catch (e: Exception) {
            Toast.makeText(this, "图片处理失败: ${e.message}", Toast.LENGTH_SHORT).show()
            savedImagePath = null
        }
    }

    private fun publishPost() {
        val content = binding.etContent.text.toString().trim()

        // 验证输入
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入帖子内容", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.length > maxWordCount) {
            Toast.makeText(this, "内容不能超过$maxWordCount", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = UserManager.getCurrentUserNickname(this) ?: "骑行者"
        val currentUserAvatar = UserManager.getCurrentUserAvatar(this) ?: "ic_twotone_person_24"


        // 创建帖子实体
        val newPost = CommunityPostEntity(
            userNickname = currentUser,
            userAvatar = currentUserAvatar,
            content = content,
            imageUrl = savedImagePath
        )

        // 使用协程在后台线程插入数据库
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sportDatabase.communityPostDao().insertPost(newPost)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreatePostActivity, "发布成功", Toast.LENGTH_SHORT).show()
                    finish() // 发布成功后关闭页面
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreatePostActivity, "发布失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createNewPost(content: String): Post {
        return Post(
            id = UUID.randomUUID().toString(),
            userId = "current_user",
            userName = "骑行爱好者", // 这里应该从用户信息获取
            userAvatar = "https://example.com/avatar1.jpg",
            content = content,
            imageUrl = selectedImageUri?.toString(), // 实际应用中需要上传图片到服务器
            timestamp = Date(),
            likes = 0,
            comments = 0,
            isLiked = false
        )
    }

    override fun onBackPressed() {
        val content = binding.etContent.text.toString().trim()
        if (content.isNotEmpty() || selectedImageUri != null) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
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
}