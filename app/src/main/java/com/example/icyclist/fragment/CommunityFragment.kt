package com.example.icyclist.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.R
import com.example.icyclist.adapter.CommunityPostAdapter
import com.example.icyclist.community.CreatePostActivity
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.SportDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton
    private lateinit var sportDatabase: SportDatabase
    private lateinit var postAdapter: CommunityPostAdapter
    private val postList = mutableListOf<CommunityPostEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化数据库
        sportDatabase = SportDatabase.getDatabase(requireContext())

        // 初始化视图
        recyclerView = view.findViewById(R.id.recycler_view_posts)
        fabCreatePost = view.findViewById(R.id.fab_create_post)

        // 设置 RecyclerView
        setupRecyclerView()

        // 设置悬浮按钮点击事件
        fabCreatePost.setOnClickListener {
            val intent = Intent(requireActivity(), CreatePostActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到该页面时，都重新加载帖子
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(postList)
        recyclerView.adapter = postAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter.onDeleteClickListener = { post ->
            showDeleteConfirmationDialog(post)
        }
    }

    private fun showDeleteConfirmationDialog(post: CommunityPostEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除动态")
            .setMessage("确定要删除这条动态吗？")
            .setPositiveButton("删除") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deletePost(post: CommunityPostEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sportDatabase.communityPostDao().deletePost(post)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                    loadPosts() // 重新加载列表
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "删除失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadPosts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val posts = sportDatabase.communityPostDao().getAllPosts()
            withContext(Dispatchers.Main) {
                postList.clear()
                postList.addAll(posts)
                postAdapter.notifyDataSetChanged()
            }
        }
    }
}
