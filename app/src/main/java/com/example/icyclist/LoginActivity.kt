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
import com.example.icyclist.network.model.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 如果已登录,直接跳转主页
        if (UserManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainContainerActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        tilEmail.error = null
        tilPassword.error = null

        if (email.isEmpty()) {
            tilEmail.error = "请输入邮箱"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "请输入有效的邮箱地址"
            return false
        }

        if (password.isEmpty()) {
            tilPassword.error = "请输入密码"
            return false
        }

        return true
    }

    private fun performLogin() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        // 禁用按钮，防止重复点击
        btnLogin.isEnabled = false
        btnLogin.text = "登录中..."

        lifecycleScope.launch {
            try {
                // 调用后端登录 API
                val apiService = RetrofitClient.getApiService(this@LoginActivity)
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    
                    // 保存 JWT Token 和用户信息
                    UserManager.saveAuthToken(this@LoginActivity, loginResponse.token)
                    UserManager.saveUserId(this@LoginActivity, loginResponse.user?.id ?: 0)
                    
                    // 保存登录状态（使用UserManager统一管理）
                    UserManager.setLoggedIn(
                        this@LoginActivity, 
                        email, 
                        loginResponse.user?.nickname ?: email.substringBefore("@")
                    )
                    
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainContainerActivity::class.java))
                    finish()
                } else {
                    // 登录失败
                    tilPassword.error = "邮箱或密码错误"
                    btnLogin.isEnabled = true
                    btnLogin.text = "登录"
                }
            } catch (e: Exception) {
                // 网络错误
                android.util.Log.e("LoginActivity", "登录失败", e)
                Toast.makeText(this@LoginActivity, "登录失败: ${e.message}", Toast.LENGTH_LONG).show()
                btnLogin.isEnabled = true
                btnLogin.text = "登录"
            }
        }
    }
}
