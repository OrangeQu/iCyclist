package com.example.icyclist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.icyclist.R
import com.example.icyclist.database.CommentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 评论列表适配器
 */
class CommentAdapter(
    private val comments: List<CommentEntity>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_comment_avatar)
        val tvNickname: TextView = itemView.findViewById(R.id.tv_comment_nickname)
        val tvContent: TextView = itemView.findViewById(R.id.tv_comment_content)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_comment_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.tvNickname.text = comment.userNickname
        holder.tvContent.text = comment.content
        holder.tvTimestamp.text = formatTimestamp(comment.timestamp)

        // Load user avatar
        val avatarResourceId = holder.itemView.context.resources.getIdentifier(
            comment.userAvatar, "drawable", holder.itemView.context.packageName
        )
        if (avatarResourceId != 0) {
            Glide.with(holder.itemView.context)
                .load(avatarResourceId)
                .circleCrop()
                .into(holder.ivAvatar)
        }
    }

    override fun getItemCount(): Int = comments.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}


