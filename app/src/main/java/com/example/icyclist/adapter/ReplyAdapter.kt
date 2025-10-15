package com.example.icyclist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.R

data class Reply(
    val id: Int,
    val authorName: String,
    val content: String
)

class ReplyAdapter(private val replies: List<Reply>) :
    RecyclerView.Adapter<ReplyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val authorName: TextView = view.findViewById(R.id.replyAuthorName)
        val content: TextView = view.findViewById(R.id.replyContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_reply, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replies[position]
        holder.authorName.text = reply.authorName
        holder.content.text = reply.content
    }

    override fun getItemCount() = replies.size
}
