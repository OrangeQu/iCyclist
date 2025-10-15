package com.example.icyclist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.adapter.ForumCategory
import com.example.icyclist.adapter.ForumCategoryAdapter
import com.example.icyclist.databinding.FragmentForumCategoriesBinding

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
    }

    private fun setupRecyclerView() {
        val categories = listOf(
            ForumCategory(1, "装备讨论", "分享和讨论骑行装备", 128),
            ForumCategory(2, "路线分享", "推荐你最喜欢的骑行路线", 96),
            ForumCategory(3, "新手问答", "新手上路？在这里提问吧", 256),
            ForumCategory(4, "骑闻轶事", "分享骑行中的趣闻和故事", 78),
            ForumCategory(5, "二手交易", "买卖你的闲置骑行装备", 42)
        )

        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecyclerView.adapter = ForumCategoryAdapter(categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
