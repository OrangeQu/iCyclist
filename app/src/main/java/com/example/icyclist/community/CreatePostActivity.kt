package com.example.icyclist.community

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
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
        handleIncomingImage()
    }

    private fun handleIncomingImage() {
        if (intent.hasExtra("image_uri")) {
            val imageUriString = intent.getStringExtra("image_uri")
            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                selectedImageUri = imageUri
                showSelectedImage(imageUri)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
        // 使用 Glide 加载图片，避免 OOM
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.ivSelectedImage)
        
        binding.ivSelectedImage.visibility = View.VISIBLE
        binding.btnRemoveImage.visibility = View.VISIBLE
        
        // 在后台线程保存图片
        saveImageToInternalStorage(uri)
    }

    private fun removeSelectedImage() {
        selectedImageUri = null
        savedImagePath = null
        binding.ivSelectedImage.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.GONE
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
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
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreatePostActivity, "图片已选择", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreatePostActivity, "图片处理失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    savedImagePath = null
                    // 隐藏图片预览
                    binding.ivSelectedImage.visibility = View.GONE
                    binding.btnRemoveImage.visibility = View.GONE
                }
            }
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

        // 禁用按钮，防止重复点击
        binding.btnPublish.isEnabled = false
        binding.btnPublish.text = "发布中..."

        lifecycleScope.launch {
            try {
                // 创建网络请求对象
                val postRequest = com.example.icyclist.network.model.Post(
                    content = content,
                    mediaUrls = if (savedImagePath != null) listOf(savedImagePath!!) else emptyList()
                )
                
                // 提交到服务器
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@CreatePostActivity)
                val response = apiService.createPost(postRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val createdPost = response.body()!!
                    
                    // 同时保存到本地缓存
                    withContext(Dispatchers.IO) {
                        val postEntity = CommunityPostEntity(
                            id = createdPost.id?.toInt() ?: 0,
                            userNickname = createdPost.authorName,
                            userAvatar = createdPost.authorAvatar,
                            content = createdPost.content ?: "",
                            imageUrl = createdPost.imageUrls?.firstOrNull(),
                            timestamp = System.currentTimeMillis(),
                            likes = createdPost.likeCount,
                            comments = createdPost.commentCount
                        )
                        sportDatabase.communityPostDao().insertPost(postEntity)
                    }
                    
                    Toast.makeText(this@CreatePostActivity, "发布成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreatePostActivity, "发布失败: ${response.code()}", Toast.LENGTH_LONG).show()
                    binding.btnPublish.isEnabled = true
                    binding.btnPublish.text = "发布"
                }
            } catch (e: Exception) {
                android.util.Log.e("CreatePostActivity", "发布失败", e)
                Toast.makeText(this@CreatePostActivity, "发布失败: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "发布"
            }
        }
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