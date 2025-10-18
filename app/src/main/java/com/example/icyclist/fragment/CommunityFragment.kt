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
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.FragmentForumCategoriesBinding
import com.example.icyclist.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityFragment : Fragment() {

    private var _binding: FragmentForumCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sportDatabase: SportDatabase
    private var isLoading = false  // 防止重复加载
    private var loadingJob: Job? = null  // 跟踪数据加载任务

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sportDatabase = SportDatabase.getDatabase(requireContext())
        
        setupRecyclerView()
        loadForumCategories()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到这个界面时刷新数据（仅在视图已创建时）
        if (_binding != null && !isLoading) {
            loadForumCategories()
        }
    }

    private fun setupRecyclerView() {
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * 从服务器加载论坛分类数据（带本地缓存）
     */
    private fun loadForumCategories() {
        // 取消之前的加载任务
        loadingJob?.cancel()
        
        // 防止重复加载
        if (isLoading) return
        isLoading = true
        
        android.util.Log.d("CommunityFragment", "🌐 从服务器加载论坛分类...")
        loadingJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 检查Fragment是否还在活动状态
                if (!isAdded || _binding == null) {
                    isLoading = false
                    return@launch
                }
                // 先从服务器获取数据
                val apiService = RetrofitClient.getApiService(requireContext())
                val response = apiService.getForumCategories()
                
                if (response.isSuccessful && response.body() != null) {
                    val networkCategories = response.body()!!
                    
                    // 转换为 Adapter 需要的格式，并从本地数据库获取实际主题数量
                    val categories = networkCategories.map { netCategory ->
                        val categoryId = netCategory.id?.toInt() ?: 0
                        // 从本地数据库查询实际的主题数量
                        val actualTopicCount = withContext(Dispatchers.IO) {
                            sportDatabase.forumTopicDao().getTopicCountByCategory(categoryId)
                        }
                        ForumCategory(
                            id = categoryId,
                            name = netCategory.name,
                            description = netCategory.description ?: "",
                            postCount = actualTopicCount  // 使用实际查询到的数量
                        )
                    }
                    
                    // 更新 UI
                    if (_binding != null) {
                        binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(categories)
                    }
                    
                    // 可选：保存到本地数据库作为缓存
                    withContext(Dispatchers.IO) {
                        networkCategories.forEach { netCategory ->
                            val entity = com.example.icyclist.database.ForumCategoryEntity(
                                id = netCategory.id?.toInt() ?: 0,
                                name = netCategory.name,
                                description = netCategory.description ?: "",
                                topicCount = netCategory.topicCount
                            )
                            sportDatabase.forumCategoryDao().insertCategory(entity)
                        }
                    }
                    
                    android.util.Log.d("CommunityFragment", "✅ 从服务器加载成功！共 ${categories.size} 个分类")
                    isLoading = false
                } else {
                    // 服务器请求失败，从本地数据库加载
                    android.util.Log.w("CommunityFragment", "服务器请求失败，从本地缓存加载")
                    loadFromLocalCache()
                }
            } catch (e: Exception) {
                // 网络错误，从本地数据库加载
                android.util.Log.e("CommunityFragment", "网络错误，从本地缓存加载: ${e.message}", e)
                loadFromLocalCache()
            }
        }
    }
    
    /**
     * 从本地缓存加载数据（作为后备方案）
     */
    private fun loadFromLocalCache() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 检查Fragment是否还在活动状态
                if (!isAdded || _binding == null) {
                    isLoading = false
                    return@launch
                }
                
                val dbCategories = withContext(Dispatchers.IO) {
                    sportDatabase.forumCategoryDao().getAllCategories()
                }
                
                val categories = dbCategories.map { dbCategory ->
                    val actualTopicCount = withContext(Dispatchers.IO) {
                        sportDatabase.forumTopicDao().getTopicCountByCategory(dbCategory.id)
                    }
                    ForumCategory(
                        id = dbCategory.id,
                        name = dbCategory.name,
                        description = dbCategory.description,
                        postCount = actualTopicCount
                    )
                }
                
                if (_binding != null) {
                    binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(categories)
                }
                android.util.Log.d("CommunityFragment", "从本地缓存加载 ${categories.size} 个分类")
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("CommunityFragment", "加载失败: ${e.message}", e)
                if (isAdded && _binding != null) {
                    Toast.makeText(requireContext(), "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // 取消所有正在进行的数据加载任务
        loadingJob?.cancel()
        loadingJob = null
        // 重置加载状态，避免在切换Fragment时阻塞后续加载
        isLoading = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
