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
import com.example.icyclist.database.LikeEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.FragmentMomentBinding
import com.example.icyclist.manager.UserManager
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
        setupSwipeRefresh()
        
        // 直接从本地数据库加载数据
        loadPostsFromLocalDatabase()
    }

    override fun onResume() {
        super.onResume()
        // 每次回到这个界面时刷新数据
        loadPostsFromLocalDatabase()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(posts).apply {
            onDeleteClickListener = { post ->
                deletePost(post)
            }
            onLikeClickListener = { post ->
                handleLike(post)
            }
            onCommentClickListener = { post ->
                handleComment(post)
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
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        
        binding.swipeRefresh.setOnRefreshListener {
            // 下拉刷新时重新加载数据
            loadPostsFromLocalDatabase()
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
     * 从服务器加载帖子列表（带本地缓存）
     */
    private fun loadPostsFromLocalDatabase() {
        lifecycleScope.launch {
            try {
                // 先从服务器获取数据
                val apiService = RetrofitClient.getApiService(requireContext())
                val response = apiService.getTimelinePosts()
                
                if (response.isSuccessful && response.body() != null) {
                    val networkPosts = response.body()!!
                    
                    // 转换为本地实体格式
                    val localPosts = networkPosts.map { networkPost ->
                        convertNetworkPostToLocal(networkPost)
                    }
                    
                    // 更新 UI
                    posts.clear()
                    posts.addAll(localPosts)
                    postAdapter?.notifyDataSetChanged()
                    
                    // 保存到本地缓存
                    withContext(Dispatchers.IO) {
                        localPosts.forEach { post ->
                            sportDatabase.communityPostDao().insertPost(post)
                        }
                    }
                    
                    android.util.Log.d("MomentFragment", "✅ 从服务器加载 ${posts.size} 条帖子")
                    
                    // 停止刷新动画
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    // 服务器请求失败，从本地缓存加载
                    android.util.Log.w("MomentFragment", "服务器请求失败，从本地缓存加载")
                    loadFromLocalCache()
                }
            } catch (e: Exception) {
                // 网络错误，从本地缓存加载
                android.util.Log.e("MomentFragment", "网络错误: ${e.message}", e)
                loadFromLocalCache()
            }
        }
    }
    
    /**
     * 从本地缓存加载（作为后备方案）
     */
    private fun loadFromLocalCache() {
        lifecycleScope.launch {
            try {
                val localPosts = withContext(Dispatchers.IO) {
                    sportDatabase.communityPostDao().getAllPosts()
                }
                
                posts.clear()
                posts.addAll(localPosts)
                
                // 加载每个帖子的点赞和评论数据
                val currentUserId = UserManager.getCurrentUserEmail(requireContext()) ?: ""
                val postsCopy = posts.toList()
                
                postsCopy.forEach { post ->
                    val likeCount = withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().getLikeCount(post.id)
                    }
                    val isLiked = withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().isLiked(post.id, currentUserId)
                    }
                    val commentCount = withContext(Dispatchers.IO) {
                        sportDatabase.commentDao().getCommentCount(post.id)
                    }
                    
                    postAdapter?.updateLikeState(post.id, isLiked, likeCount)
                    postAdapter?.updateCommentCount(post.id, commentCount)
                }
                
                postAdapter?.notifyDataSetChanged()
                android.util.Log.d("MomentFragment", "从本地缓存加载 ${posts.size} 条帖子")
                
                // 停止刷新动画
                binding.swipeRefresh.isRefreshing = false
            } catch (e: Exception) {
                android.util.Log.e("MomentFragment", "加载帖子失败", e)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // 停止刷新动画
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    /**
     * 处理点赞/取消点赞
     */
    private fun handleLike(post: CommunityPostEntity) {
        lifecycleScope.launch {
            try {
                val currentUserId = UserManager.getCurrentUserEmail(requireContext()) ?: ""
                
                val isLiked = withContext(Dispatchers.IO) {
                    sportDatabase.likeDao().isLiked(post.id, currentUserId)
                }
                
                if (isLiked) {
                    // 取消点赞
                    withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().deleteLike(post.id, currentUserId)
                    }
                } else {
                    // 点赞
                    val like = LikeEntity(
                        postId = post.id,
                        userId = currentUserId
                    )
                    withContext(Dispatchers.IO) {
                        sportDatabase.likeDao().insertLike(like)
                    }
                }
                
                // 更新UI
                val newLikeCount = withContext(Dispatchers.IO) {
                    sportDatabase.likeDao().getLikeCount(post.id)
                }
                val newIsLiked = withContext(Dispatchers.IO) {
                    sportDatabase.likeDao().isLiked(post.id, currentUserId)
                }
                
                postAdapter?.updateLikeState(post.id, newIsLiked, newLikeCount)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 处理评论按钮点击，打开帖子详情页面
     */
    private fun handleComment(post: CommunityPostEntity) {
        val intent = Intent(requireContext(), com.example.icyclist.PostDetailActivity::class.java)
        intent.putExtra(com.example.icyclist.PostDetailActivity.EXTRA_POST_ID, post.id)
        startActivity(intent)
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

