package com.example.icyclist

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.icyclist.manager.UserManager
import com.example.icyclist.network.RetrofitClient
import com.example.icyclist.network.model.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilNickname: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPasswordConfirm: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etNickname: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPasswordConfirm: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilNickname = findViewById(R.id.tilNickname)
        tilPassword = findViewById(R.id.tilPassword)
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm)
        etEmail = findViewById(R.id.etEmail)
        etNickname = findViewById(R.id.etNickname)
        etPassword = findViewById(R.id.etPassword)
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegister()
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val nickname = etNickname.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val passwordConfirm = etPasswordConfirm.text?.toString().orEmpty()

        tilEmail.error = null
        tilNickname.error = null
        tilPassword.error = null
        tilPasswordConfirm.error = null

        if (email.isEmpty()) {
            tilEmail.error = "请输入邮箱"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "请输入有效的邮箱地址"
            return false
        }

        if (nickname.isEmpty()) {
            tilNickname.error = "请输入昵称"
            return false
        }

        if (nickname.length < 2) {
            tilNickname.error = "昵称至少需要2位"
            return false
        }

        if (password.isEmpty()) {
            tilPassword.error = "请输入密码"
            return false
        }

        if (password.length < 6) {
            tilPassword.error = "密码至少需要6位"
            return false
        }

        if (passwordConfirm.isEmpty()) {
            tilPasswordConfirm.error = "请确认密码"
            return false
        }

        if (password != passwordConfirm) {
            tilPasswordConfirm.error = "两次输入的密码不一致"
            return false
        }

        return true
    }

    private fun performRegister() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val nickname = etNickname.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        // 禁用按钮，防止重复点击
        btnRegister.isEnabled = false
        btnRegister.text = "注册中..."

        lifecycleScope.launch {
            try {
                // 调用后端注册 API
                val apiService = RetrofitClient.getApiService(this@RegisterActivity)
                val registerRequest = RegisterRequest(email, password, nickname)
                
                android.util.Log.d("RegisterActivity", "📤 发送注册请求: $registerRequest")
                val response = apiService.register(registerRequest)
                
                android.util.Log.d("RegisterActivity", "📥 收到响应: code=${response.code()}, isSuccessful=${response.isSuccessful}")
                android.util.Log.d("RegisterActivity", "📥 响应body: ${response.body()}")
                android.util.Log.d("RegisterActivity", "📥 错误body: ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body() != null) {
                    // 注册成功
                    android.util.Log.d("RegisterActivity", "✅ 注册成功")
                    Toast.makeText(this@RegisterActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    // 注册失败（如邮箱已存在）
                    android.util.Log.e("RegisterActivity", "❌ 注册失败: code=${response.code()}")
                    val errorMessage = when (response.code()) {
                        400 -> "该邮箱已被注册"
                        else -> "注册失败(${response.code()})，请稍后重试"
                    }
                    tilEmail.error = errorMessage
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    btnRegister.isEnabled = true
                    btnRegister.text = "注册"
                }
            } catch (e: Exception) {
                // 网络错误
                android.util.Log.e("RegisterActivity", "💥 异常: ${e.javaClass.simpleName}: ${e.message}", e)
                Toast.makeText(this@RegisterActivity, "网络错误: ${e.message}", Toast.LENGTH_LONG).show()
                btnRegister.isEnabled = true
                btnRegister.text = "注册"
            }
        }
    }
}
