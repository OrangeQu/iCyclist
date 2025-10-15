package com.example.icyclist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.R
import com.example.icyclist.community.TopicDetailActivity

data class Topic(
    val id: Int,
    val title: String,
    val authorName: String,
    val replyCount: Int
)

class TopicAdapter(private val topics: List<Topic>) :
    RecyclerView.Adapter<TopicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topicTitle: TextView = view.findViewById(R.id.topicTitle)
        val authorName: TextView = view.findViewById(R.id.authorName)
        val replyCount: TextView = view.findViewById(R.id.replyCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]
        holder.topicTitle.text = topic.title
        holder.authorName.text = topic.authorName
        holder.replyCount.text = "${topic.replyCount} 回复"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TopicDetailActivity::class.java).apply {
                putExtra("TOPIC_TITLE", topic.title)
                // You can also pass topic.id to fetch specific data later
                // putExtra("TOPIC_ID", topic.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = topics.size
}
