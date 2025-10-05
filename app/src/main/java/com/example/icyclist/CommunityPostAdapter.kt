package com.example.icyclist

import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.database.CommunityPostEntity
import java.io.File

class CommunityPostAdapter(
    private val posts: MutableList<CommunityPostEntity>
) : RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserAvatar: ImageView = itemView.findViewById(R.id.imgUserAvatar)
        val tvUserNickname: TextView = itemView.findViewById(R.id.tvUserNickname)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        holder.tvUserNickname.text = post.userNickname
        holder.tvContent.text = post.content
        
        // 显示相对时间
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            post.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvPostTime.text = relativeTime
        
        // 加载用户头像
        if (!post.userAvatarPath.isNullOrEmpty()) {
            val avatarFile = File(post.userAvatarPath)
            if (avatarFile.exists()) {
                holder.imgUserAvatar.setImageURI(Uri.fromFile(avatarFile))
            }
        }
        
        // 加载缩略图
        if (!post.thumbnailPath.isNullOrEmpty()) {
            val thumbnailFile = File(post.thumbnailPath)
            if (thumbnailFile.exists()) {
                holder.imgThumbnail.visibility = View.VISIBLE
                holder.imgThumbnail.setImageURI(Uri.fromFile(thumbnailFile))
            } else {
                holder.imgThumbnail.visibility = View.GONE
            }
        } else {
            holder.imgThumbnail.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<CommunityPostEntity>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}
