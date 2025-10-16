package com.example.icyclist.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.CommunityPostAdapter
import com.example.icyclist.community.CreatePostActivity
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.FragmentMomentBinding
import com.example.icyclist.network.RetrofitClient
import com.example.icyclist.network.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 骑行圈（动态）Fragment
 * 显示所有用户的骑行分享帖子
 */
class MomentFragment : Fragment() {

    private var _binding: FragmentMomentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sportDatabase: SportDatabase
    private var postAdapter: CommunityPostAdapter? = null
    private val posts = mutableListOf<CommunityPostEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMomentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sportDatabase = SportDatabase.getDatabase(requireContext())
        
        setupRecyclerView()
        setupFab()
        
        // 从网络加载数据
        loadPostsFromNetwork()
    }

    override fun onResume() {
        super.onResume()
        // 每次回到这个界面时刷新数据
        loadPostsFromNetwork()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(posts).apply {
            onDeleteClickListener = { post ->
                deletePost(post)
            }
        }
        
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupFab() {
        binding.fabCreatePost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 从网络加载骑行圈帖子数据
     */
    private fun loadPostsFromNetwork() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(requireContext())
                val response = apiService.getTimelinePosts()
                
                if (response.isSuccessful) {
                    val networkPosts = response.body() ?: emptyList()
                    
                    // 将网络数据转换为本地数据格式
                    val localPosts = networkPosts.map { networkPost ->
                        convertNetworkPostToLocal(networkPost)
                    }
                    
                    // 更新 UI
                    posts.clear()
                    posts.addAll(localPosts)
                    postAdapter?.notifyDataSetChanged()
                    
                    // 可选：同时保存到本地数据库作为缓存
                    // savePostsToLocalDatabase(localPosts)
                } else {
                    // 请求失败，从本地数据库加载
                    showErrorAndLoadLocalData("加载失败: ${response.code()}")
                }
            } catch (e: Exception) {
                // 网络错误，从本地数据库加载
                showErrorAndLoadLocalData("网络错误: ${e.message}")
            }
        }
    }

    /**
     * 将网络帖子数据转换为本地实体
     */
    private fun convertNetworkPostToLocal(networkPost: Post): CommunityPostEntity {
        return CommunityPostEntity(
            id = networkPost.id?.toInt() ?: 0,
            userNickname = networkPost.user?.nickname ?: "匿名用户",
            userAvatar = networkPost.user?.avatar ?: "ic_twotone_person_24",
            content = networkPost.content ?: "",
            imageUrl = networkPost.mediaUrls?.firstOrNull(),
            timestamp = System.currentTimeMillis(),
            sportRecordId = networkPost.rideRecordId,
            sportDistance = null,
            sportDuration = null,
            sportThumbPath = null
        )
    }

    /**
     * 显示错误并从本地数据库加载数据
     */
    private fun showErrorAndLoadLocalData(errorMessage: String) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        loadPostsFromLocalDatabase()
    }

    /**
     * 从本地数据库加载帖子（作为后备方案）
     */
    private fun loadPostsFromLocalDatabase() {
        lifecycleScope.launch {
            val localPosts = withContext(Dispatchers.IO) {
                sportDatabase.communityPostDao().getAllPosts()
            }
            
            posts.clear()
            posts.addAll(localPosts)
            postAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * 删除帖子
     */
    private fun deletePost(post: CommunityPostEntity) {
        lifecycleScope.launch {
            try {
                // 从本地数据库删除
                withContext(Dispatchers.IO) {
                    sportDatabase.communityPostDao().deletePost(post)
                }
                
                // 更新 UI
                posts.remove(post)
                postAdapter?.notifyDataSetChanged()
                
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                
                // TODO: 如果需要，也可以调用后端 API 删除服务器端的数据
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

