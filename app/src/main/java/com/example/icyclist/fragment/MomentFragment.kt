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
import com.example.icyclist.adapter.CommunityPostAdapter
import com.example.icyclist.community.CreatePostActivity
import com.example.icyclist.database.CommunityPostEntity
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.databinding.FragmentMomentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MomentFragment : Fragment() {
    private var _binding: FragmentMomentBinding? = null
    private val binding get() = _binding!!

    private lateinit var sportDatabase: SportDatabase
    private lateinit var postAdapter: CommunityPostAdapter
    private val postList = mutableListOf<CommunityPostEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMomentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sportDatabase = SportDatabase.getDatabase(requireContext())
        setupRecyclerView()
        binding.fabCreatePost.setOnClickListener {
            val intent = Intent(requireActivity(), CreatePostActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(postList)
        binding.recyclerViewPosts.adapter = postAdapter
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        postAdapter.onDeleteClickListener = { post ->
            showDeleteConfirmationDialog(post)
        }
    }

    private fun showDeleteConfirmationDialog(post: CommunityPostEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除动态")
            .setMessage("确定要删除这条动态吗？")
            .setPositiveButton("删除") { _, _ -> deletePost(post) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deletePost(post: CommunityPostEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sportDatabase.communityPostDao().deletePost(post)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                    loadPosts()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
