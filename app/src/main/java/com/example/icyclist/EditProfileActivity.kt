package com.example.icyclist

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.icyclist.manager.UserManager
import com.example.icyclist.network.RetrofitClient
import com.example.icyclist.network.model.ProfileRequest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgAvatar: ImageView
    private lateinit var btnChangeAvatar: MaterialButton
    private lateinit var tilNickname: TextInputLayout
    private lateinit var etNickname: TextInputEditText
    private lateinit var tvEmail: TextView
    private lateinit var btnSave: MaterialButton

    private var currentAvatarPath: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveAvatarImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        imgAvatar = findViewById(R.id.imgAvatar)
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar)
        tilNickname = findViewById(R.id.tilNickname)
        etNickname = findViewById(R.id.etNickname)
        tvEmail = findViewById(R.id.tvEmail)
        btnSave = findViewById(R.id.btnSave)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserData() {
        val email = UserManager.getCurrentUserEmail(this)
        val nickname = UserManager.getCurrentUserNickname(this)
        val avatarPath = UserManager.getCurrentUserAvatar(this)

        tvEmail.text = email ?: ""
        etNickname.setText(nickname ?: "")

        currentAvatarPath = avatarPath
        loadAvatar(avatarPath)
    }

    private fun loadAvatar(avatarPath: String?) {
        if (!avatarPath.isNullOrEmpty() && avatarPath.startsWith("/")) {
            // 自定义头像路径
            val file = File(avatarPath)
            if (file.exists()) {
                // 移除padding和背景
                imgAvatar.setPadding(0, 0, 0, 0)
                imgAvatar.background = null
                
                Glide.with(this)
                    .load(file)
                    .centerCrop()
                    .into(imgAvatar)
            } else {
                // 文件不存在，显示默认头像
                loadDefaultAvatar()
            }
        } else {
            // 默认头像（icon）
            loadDefaultAvatar()
        }
    }

    private fun loadDefaultAvatar() {
        imgAvatar.setImageResource(R.drawable.ic_twotone_person_24)
        val paddingPx = (32 * resources.displayMetrics.density).toInt()
        imgAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        imgAvatar.setBackgroundResource(R.drawable.avatar_background)
    }

    private fun setupListeners() {
        btnChangeAvatar.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveAvatarImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val avatarDir = File(filesDir, "avatars")
            if (!avatarDir.exists()) {
                avatarDir.mkdirs()
            }

            val avatarFile = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(avatarFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            currentAvatarPath = avatarFile.absolutePath
            
            // 移除padding和背景，显示自定义头像
            imgAvatar.setPadding(0, 0, 0, 0)
            imgAvatar.background = null
            
            // 使用Glide加载图片，自动适配圆形
            Glide.with(this)
                .load(avatarFile)
                .centerCrop()
                .into(imgAvatar)
            
        } catch (e: Exception) {
            Toast.makeText(this, "头像保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile() {
        val nickname = etNickname.text?.toString()?.trim().orEmpty()

        tilNickname.error = null

        if (nickname.isEmpty()) {
            tilNickname.error = "请输入昵称"
            return
        }

        if (nickname.length < 2) {
            tilNickname.error = "昵称至少需要2位"
            return
        }

        // 禁用按钮防止重复点击
        btnSave.isEnabled = false
        btnSave.text = "保存中..."

        lifecycleScope.launch {
            try {
                val userId = UserManager.getUserId(this@EditProfileActivity)
                if (userId != null && userId > 0) {
                    // 提交到服务器
                    val apiService = RetrofitClient.getApiService(this@EditProfileActivity)
                    val profileRequest = ProfileRequest(
                        nickname = nickname,
                        avatar = currentAvatarPath
                    )
                    
                    val response = apiService.updateUserProfile(userId, profileRequest)
                    
                    if (response.isSuccessful && response.body() != null) {
                        // 服务器更新成功，同步到本地
                        UserManager.updateNickname(this@EditProfileActivity, nickname)
                        currentAvatarPath?.let {
                            UserManager.updateAvatar(this@EditProfileActivity, it)
                        }
                        
                        Toast.makeText(this@EditProfileActivity, "个人资料已保存", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        // 服务器更新失败
                        Toast.makeText(this@EditProfileActivity, "保存失败，请重试", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                        btnSave.text = "保存"
                    }
                } else {
                    // 未登录，仅保存到本地
                    UserManager.updateNickname(this@EditProfileActivity, nickname)
                    currentAvatarPath?.let {
                        UserManager.updateAvatar(this@EditProfileActivity, it)
                    }
                    Toast.makeText(this@EditProfileActivity, "个人资料已保存（仅本地）", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditProfileActivity", "保存资料失败", e)
                Toast.makeText(this@EditProfileActivity, "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
                btnSave.text = "保存"
            }
        }
    }
}
