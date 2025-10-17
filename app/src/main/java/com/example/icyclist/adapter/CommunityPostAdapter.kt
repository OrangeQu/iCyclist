package com.example.icyclist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.icyclist.R
import com.example.icyclist.database.CommunityPostEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityPostAdapter(
    private val posts: List<CommunityPostEntity>
) : RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    var onDeleteClickListener: ((CommunityPostEntity) -> Unit)? = null
    var onLikeClickListener: ((CommunityPostEntity) -> Unit)? = null
    var onCommentClickListener: ((CommunityPostEntity) -> Unit)? = null
    
    // 用于存储每个帖子的点赞状态和数量
    private val likeStates = mutableMapOf<Int, Boolean>()  // postId -> isLiked
    private val likeCounts = mutableMapOf<Int, Int>()      // postId -> likeCount
    private val commentCounts = mutableMapOf<Int, Int>()   // postId -> commentCount

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val tvNickname: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_post_time)
        val tvContent: TextView = itemView.findViewById(R.id.tv_post_content)
        val ivPostImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        val ivPostOptions: ImageButton = itemView.findViewById(R.id.iv_post_options)
        val btnLike: ImageButton = itemView.findViewById(R.id.btn_like)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        val btnComment: ImageButton = itemView.findViewById(R.id.btn_comment)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
    }
    
    /**
     * 更新点赞状态
     */
    fun updateLikeState(postId: Int, isLiked: Boolean, likeCount: Int) {
        likeStates[postId] = isLiked
        likeCounts[postId] = likeCount
        notifyDataSetChanged()
    }
    
    /**
     * 更新评论数量
     */
    fun updateCommentCount(postId: Int, commentCount: Int) {
        commentCounts[postId] = commentCount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvNickname.text = post.userNickname
        holder.tvContent.text = post.content
        holder.tvTimestamp.text = formatTimestamp(post.timestamp)
        
        // Load user avatar (assuming it's a drawable resource name for now)
        val avatarResourceId = holder.itemView.context.resources.getIdentifier(
            post.userAvatar, "drawable", holder.itemView.context.packageName
        )
        if (avatarResourceId != 0) {
            Glide.with(holder.itemView.context)
                .load(avatarResourceId)
                .circleCrop()
                .into(holder.ivAvatar)
        }

        holder.ivPostOptions.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete_post -> {
                        onDeleteClickListener?.invoke(post)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Load post image if it exists
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }
        
        // 设置点赞状态和数量
        val isLiked = likeStates[post.id] ?: false
        val likeCount = likeCounts[post.id] ?: post.likes
        holder.tvLikeCount.text = likeCount.toString()
        
        // 更新点赞按钮图标
        if (isLiked) {
            holder.btnLike.setImageResource(R.drawable.ic_liked)
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_like)
        }
        
        // 点赞按钮点击事件
        holder.btnLike.setOnClickListener {
            onLikeClickListener?.invoke(post)
        }
        
        // 设置评论数量
        val commentCount = commentCounts[post.id] ?: post.comments
        holder.tvCommentCount.text = commentCount.toString()
        
        // 评论按钮点击事件
        holder.btnComment.setOnClickListener {
            onCommentClickListener?.invoke(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
