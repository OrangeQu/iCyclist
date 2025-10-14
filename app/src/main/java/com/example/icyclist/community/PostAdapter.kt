package com.example.icyclist.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.icyclist.R
import com.example.icyclist.community.Post
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private var posts: List<Post>,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }
    fun getCurrentPosts(): List<Post> {
        return posts
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val postTime: TextView = itemView.findViewById(R.id.tv_post_time)
        private val postContent: TextView = itemView.findViewById(R.id.tv_post_content)
        private val postImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        private val likeButton: ImageButton = itemView.findViewById(R.id.btn_like)
        private val likeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val commentButton: ImageButton = itemView.findViewById(R.id.btn_comment)
        private val commentCount: TextView = itemView.findViewById(R.id.tv_comment_count)

        fun bind(post: Post) {
            // 加载用户头像
            Glide.with(itemView.context)
                .load(post.userAvatar)
                .placeholder(R.drawable.ic_default_avatar)
                .into(userAvatar)

            userName.text = post.userName
            postContent.text = post.content

            // 格式化时间
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            postTime.text = dateFormat.format(post.timestamp)

            // 处理图片
            if (!post.imageUrl.isNullOrEmpty()) {
                postImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(postImage)
            } else {
                postImage.visibility = View.GONE
            }

            // 点赞功能
            likeCount.text = post.likes.toString()
            likeButton.setImageResource(
                if (post.isLiked) R.drawable.ic_liked else R.drawable.ic_like
            )
            likeButton.setOnClickListener {
                onLikeClick(post)
            }

            // 评论功能
            commentCount.text = post.comments.toString()
            commentButton.setOnClickListener {
                onCommentClick(post)
            }
        }
    }
}