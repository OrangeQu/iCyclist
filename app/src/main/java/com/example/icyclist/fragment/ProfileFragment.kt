package com.example.icyclist.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.icyclist.EditProfileActivity
import com.example.icyclist.LoginActivity
import com.example.icyclist.R
import com.example.icyclist.manager.UserManager
import com.example.icyclist.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File

class ProfileFragment : Fragment() {

    private var imgAvatar: ImageView? = null
    private var tvUserName: TextView? = null
    private var tvUserEmail: TextView? = null
    
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 重新加载用户数据
            loadUserData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.profileToolbar)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        val editButton = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val logoutButton = view.findViewById<MaterialButton>(R.id.btnLogout)

        loadUserData()

        editButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        logoutButton.setOnClickListener {
            // 执行登出操作
            UserManager.logout(requireContext())
            
            // 跳转回登录页面
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 仅在视图已创建时加载数据
        if (view != null) {
            loadUserData()
        }
    }
    
    private fun loadUserData() {
        // 检查Fragment是否处于有效状态
        if (!isAdded || view == null) return
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. 优先从服务器获取
                val userId = UserManager.getUserId(requireContext())
                if (userId != null && userId > 0) {
                    val apiService = RetrofitClient.getApiService(requireContext())
                    val response = apiService.getUserProfile(userId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        
                        // 显示服务器数据
                        tvUserEmail?.text = profile.username
                        tvUserName?.text = profile.nickname ?: "骑行者"
                        
                        // 同步到本地
                        UserManager.setLoggedIn(
                            requireContext(),
                            profile.username,
                            profile.nickname
                        )
                        
                        // 加载头像
                        loadAvatar(profile.avatar)
                        
                        return@launch
                    }
                }
                
                // 2. 网络失败或未登录，使用本地缓存
                loadFromLocalCache()
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "从服务器加载用户信息失败: ${e.message}", e)
                // 降级到本地缓存
                loadFromLocalCache()
            }
        }
    }
    
    /**
     * 从本地缓存加载用户数据
     */
    private fun loadFromLocalCache() {
        val currentEmail = UserManager.getCurrentUserEmail(requireContext())
        val currentNickname = UserManager.getCurrentUserNickname(requireContext())
        val avatarPath = UserManager.getCurrentUserAvatar(requireContext())
        
        tvUserEmail?.text = currentEmail ?: ""
        tvUserName?.text = currentNickname ?: "骑行者"
        
        // 加载头像
        loadAvatar(avatarPath)
    }
    
    /**
     * 加载头像（与EditProfileActivity保持一致）
     */
    private fun loadAvatar(avatarPath: String?) {
        if (!avatarPath.isNullOrEmpty() && avatarPath.startsWith("/")) {
            // 自定义头像路径
            val file = File(avatarPath)
            if (file.exists()) {
                // 移除padding和背景
                imgAvatar?.setPadding(0, 0, 0, 0)
                imgAvatar?.background = null
                
                imgAvatar?.let { imageView ->
                    Glide.with(this)
                        .load(file)
                        .centerCrop()
                        .into(imageView)
                }
            } else {
                // 文件不存在，显示默认头像
                loadDefaultAvatar()
            }
        } else {
            // 默认头像（icon）
            loadDefaultAvatar()
        }
    }
    
    /**
     * 加载默认头像
     */
    private fun loadDefaultAvatar() {
        imgAvatar?.setImageResource(R.drawable.ic_twotone_person_24)
        val paddingPx = (24 * resources.displayMetrics.density).toInt()
        imgAvatar?.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        imgAvatar?.setBackgroundResource(R.drawable.avatar_background)
    }
}
