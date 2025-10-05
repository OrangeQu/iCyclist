package com.example.icyclist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.SportDatabase
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityFragment : Fragment() {

    private var rvCommunityPosts: RecyclerView? = null
    private var tvEmptyHint: TextView? = null
    private lateinit var communityPostAdapter: CommunityPostAdapter
    private val posts = mutableListOf<CommunityPostEntity>()
    private lateinit var sportDatabase: SportDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.communityToolbar)
        rvCommunityPosts = view.findViewById(R.id.rvCommunityPosts)
        tvEmptyHint = view.findViewById(R.id.tvEmptyHint)
        
        sportDatabase = SportDatabase.getDatabase(requireContext())
        
        // 初始化RecyclerView
        rvCommunityPosts?.layoutManager = LinearLayoutManager(requireContext())
        communityPostAdapter = CommunityPostAdapter(posts)
        rvCommunityPosts?.adapter = communityPostAdapter
        
        // 加载社区分享内容
        loadCommunityPosts()
    }
    
    override fun onResume() {
        super.onResume()
        loadCommunityPosts()
    }

    private fun loadCommunityPosts() {
        lifecycleScope.launch {
            try {
                Log.d("CommunityFragment", "开始加载社区分享...")
                
                val postEntities = withContext(Dispatchers.IO) {
                    sportDatabase.communityPostDao().getAllPosts()
                }
                
                Log.d("CommunityFragment", "从数据库获取到 ${postEntities.size} 条分享")
                
                posts.clear()
                posts.addAll(postEntities)
                communityPostAdapter.notifyDataSetChanged()
                
                // 显示/隐藏空状态提示
                if (posts.isEmpty()) {
                    tvEmptyHint?.visibility = View.VISIBLE
                    rvCommunityPosts?.visibility = View.GONE
                    Log.d("CommunityFragment", "显示空状态提示")
                } else {
                    tvEmptyHint?.visibility = View.GONE
                    rvCommunityPosts?.visibility = View.VISIBLE
                    Log.d("CommunityFragment", "显示 ${posts.size} 条分享内容")
                }
                
                // 打印每条分享的详细信息
                postEntities.forEachIndexed { index, post ->
                    Log.d("CommunityFragment", "[$index] ${post.userNickname}: ${post.content.take(30)}...")
                }
                
            } catch (e: Exception) {
                Log.e("CommunityFragment", "❌ 加载社区分享失败", e)
                e.printStackTrace()
                tvEmptyHint?.visibility = View.VISIBLE
                tvEmptyHint?.text = "加载失败: ${e.message}"
            }
        }
    }
}
