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

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val tvNickname: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_post_time)
        val tvContent: TextView = itemView.findViewById(R.id.tv_post_content)
        val ivPostImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        val ivPostOptions: ImageButton = itemView.findViewById(R.id.iv_post_options)
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
    }

    override fun getItemCount(): Int = posts.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
