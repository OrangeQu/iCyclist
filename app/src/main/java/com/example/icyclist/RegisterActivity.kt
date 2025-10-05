package com.example.icyclist

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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

        val success = UserManager.register(this, email, password, nickname)
        
        if (success) {
            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            tilEmail.error = "该邮箱已被注册"
        }
    }
}
