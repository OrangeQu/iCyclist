package com.example.icyclist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.R
import com.example.icyclist.community.TopicListActivity

data class ForumCategory(
    val id: Int,
    val name: String,
    val description: String,
    val postCount: Int
)

class ForumCategoryAdapter(private val categories: List<ForumCategory>) :
    RecyclerView.Adapter<ForumCategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val categoryDescription: TextView = view.findViewById(R.id.categoryDescription)
        val postCount: TextView = view.findViewById(R.id.postCount)
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_forum_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.name
        holder.categoryDescription.text = category.description
        holder.postCount.text = "${category.postCount}帖"
        // Here you could dynamically set the icon based on the category
        // For now, we use the default one from the layout

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TopicListActivity::class.java).apply {
                putExtra("CATEGORY_NAME", category.name)
                // You can also pass category.id to fetch specific topics later
                // putExtra("CATEGORY_ID", category.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = categories.size
}
