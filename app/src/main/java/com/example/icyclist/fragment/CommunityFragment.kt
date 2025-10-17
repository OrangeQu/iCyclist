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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityFragment : Fragment() {

    private var _binding: FragmentForumCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sportDatabase: SportDatabase

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
        // 每次回到这个界面时刷新数据（主题计数可能变化）
        loadForumCategories()
    }

    private fun setupRecyclerView() {
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * 从本地数据库加载论坛分类数据
     */
    private fun loadForumCategories() {
        android.util.Log.d("CommunityFragment", "📱 从本地数据库加载论坛分类...")
        lifecycleScope.launch {
            try {
                val dbCategories = withContext(Dispatchers.IO) {
                    sportDatabase.forumCategoryDao().getAllCategories()
                }
                
                android.util.Log.d("CommunityFragment", "✅ 加载成功！共 ${dbCategories.size} 个分类")
                
                // 将数据库数据转换为 Adapter 需要的格式，使用真实的主题计数
                val categories = dbCategories.map { dbCategory ->
                    // 动态计算实际的主题数量
                    val actualTopicCount = withContext(Dispatchers.IO) {
                        sportDatabase.forumTopicDao().getTopicCountByCategory(dbCategory.id)
                    }
                    ForumCategory(
                        id = dbCategory.id,
                        name = dbCategory.name,
                        description = dbCategory.description,
                        postCount = actualTopicCount // 使用真实的主题数量
                    )
                }
                
                // 更新 RecyclerView
                binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(categories)
                
            } catch (e: Exception) {
                android.util.Log.e("CommunityFragment", "❌ 加载失败: ${e.message}", e)
                Toast.makeText(requireContext(), "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
