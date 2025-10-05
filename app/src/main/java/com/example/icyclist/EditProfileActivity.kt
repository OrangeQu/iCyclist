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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        if (!avatarPath.isNullOrEmpty()) {
            val file = File(avatarPath)
            if (file.exists()) {
                imgAvatar.setImageURI(Uri.fromFile(file))
            }
        }
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
            imgAvatar.setImageURI(Uri.fromFile(avatarFile))
            
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

        // 保存昵称
        UserManager.updateNickname(this, nickname)

        // 保存头像路径
        currentAvatarPath?.let {
            UserManager.updateAvatar(this, it)
        }

        Toast.makeText(this, "个人资料已保存", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
