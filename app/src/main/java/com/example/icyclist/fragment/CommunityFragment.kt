package com.example.icyclist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.ForumCategory
import com.example.icyclist.adapter.ForumCategoryAdapter
import com.example.icyclist.databinding.FragmentForumCategoriesBinding
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private var _binding: FragmentForumCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadForumCategories()
    }

    private fun setupRecyclerView() {
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * 从网络加载论坛分类数据
     */
    private fun loadForumCategories() {
        android.util.Log.d("CommunityFragment", "🌐 开始加载论坛分类...")
        lifecycleScope.launch {
            try {
                android.util.Log.d("CommunityFragment", "🌐 正在请求: http://192.168.81.39:8080/api/forum/categories")
                val apiService = RetrofitClient.getApiService(requireContext())
                val response = apiService.getForumCategories()
                android.util.Log.d("CommunityFragment", "🌐 收到响应: ${response.code()}")
                
                if (response.isSuccessful) {
                    val networkCategories = response.body() ?: emptyList()
                    android.util.Log.d("CommunityFragment", "✅ 加载成功！共 ${networkCategories.size} 个分类")
                    
                    // 将网络数据转换为 Adapter 需要的格式
                    val categories = networkCategories.map { networkCategory ->
                        ForumCategory(
                            id = networkCategory.id?.toInt() ?: 0,
                            name = networkCategory.name,
                            description = networkCategory.description ?: "",
                            postCount = networkCategory.topicCount
                        )
                    }
                    
                    // 更新 RecyclerView
                    binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(categories)
                } else {
                    // 请求失败，使用假数据作为后备
                    android.util.Log.e("CommunityFragment", "❌ 加载失败: ${response.code()}")
                    showErrorAndUseFallbackData("加载失败: ${response.code()}")
                }
            } catch (e: Exception) {
                // 网络错误，使用假数据作为后备
                android.util.Log.e("CommunityFragment", "❌ 网络错误: ${e.message}", e)
                e.printStackTrace()
                showErrorAndUseFallbackData("网络错误: ${e.message}")
            }
        }
    }

    /**
     * 显示错误信息并使用后备数据
     */
    private fun showErrorAndUseFallbackData(errorMessage: String) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        
        // 使用本地假数据作为后备方案
        val fallbackCategories = listOf(
            ForumCategory(1, "装备讨论", "分享和讨论骑行装备", 128),
            ForumCategory(2, "路线分享", "推荐你最喜欢的骑行路线", 96),
            ForumCategory(3, "新手问答", "新手上路？在这里提问吧", 256),
            ForumCategory(4, "骑闻轶事", "分享骑行中的趣闻和故事", 78),
            ForumCategory(5, "二手交易", "买卖你的闲置骑行装备", 42)
        )
        
        binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(fallbackCategories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
