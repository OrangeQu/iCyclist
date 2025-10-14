package com.example.icyclist.community

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icyclist.R
import com.example.icyclist.databinding.ActivityMainBinding
import com.example.icyclist.community.Post
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation()
    }
    // 在 MainActivity 类中添加以下代码

    // 在 onCreate 方法中修改底部导航监听器
    private fun setupBottomNavigation() {
        binding.mainBottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_sport -> {
                    Toast.makeText(this, "运动", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_community -> {
                    Toast.makeText(this, "社区", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_profile -> {
                    // 打开发布页面
                    openCreatePostActivity()
                    true
                }
                R.id.menu_profile -> {
                    Toast.makeText(this, "我的", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // 添加打开发布页面的方法
    private fun openCreatePostActivity() {
        val intent = Intent(this, CreatePostActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_POST)
    }

    // 处理发布结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_POST && resultCode == Activity.RESULT_OK) {
            val newPost = data?.getSerializableExtra("new_post") as? Post
            newPost?.let {
                // 将新帖子添加到列表顶部
                val currentPosts = postAdapter.getCurrentPosts().toMutableList()
                currentPosts.add(0, it)
                postAdapter.updatePosts(currentPosts)

                // 滚动到顶部
                binding.rvSportRecords.scrollToPosition(0)

                Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 在类顶部添加常量
    companion object {
        private const val REQUEST_CREATE_POST = 1001
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            posts = getSamplePosts(),
            onLikeClick = { post ->
                // 处理点赞逻辑
                Toast.makeText(this, "点赞了 ${post.userName} 的帖子", Toast.LENGTH_SHORT).show()
            },
            onCommentClick = { post ->
                // 处理评论逻辑
                Toast.makeText(this, "评论 ${post.userName} 的帖子", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvSportRecords.layoutManager = LinearLayoutManager(this)
        binding.rvSportRecords.adapter = postAdapter
    }


    private fun getSamplePosts(): List<Post> {
        return listOf(
            Post(
                id = "1",
                userId = "user1",
                userName = "骑行爱好者",
                userAvatar = "https://example.com/avatar1.jpg",
                content = "今天沿着西湖骑行了一圈，风景太美了！推荐大家也来试试这条路线。",
                imageUrl = "https://example.com/bike1.jpg",
                timestamp = Date(),
                likes = 24,
                comments = 8,
                isLiked = false
            ),
            Post(
                id = "2",
                userId = "user2",
                userName = "山地车手",
                userAvatar = "https://example.com/avatar2.jpg",
                content = "新入手了一辆山地车，有没有周末一起爬山的骑友？",
                timestamp = Date(System.currentTimeMillis() - 3600000),
                likes = 15,
                comments = 3,
                isLiked = true
            ),
            Post(
                id = "3",
                userId = "user3",
                userName = "公路车新手",
                userAvatar = "https://example.com/avatar3.jpg",
                content = "第一次完成100公里骑行！虽然很累但是很有成就感！🚴‍♂️",
                imageUrl = "https://example.com/bike2.jpg",
                timestamp = Date(System.currentTimeMillis() - 7200000),
                likes = 42,
                comments = 12,
                isLiked = false
            )
        )
    }
}