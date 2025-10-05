package com.example.icyclist

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SportRecordAdapter(
    private val records: List<SportRecord>
) : RecyclerView.Adapter<SportRecordAdapter.RecordViewHolder>() {

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgTrackThumb: ImageView = itemView.findViewById(R.id.imgTrackThumb)
        val tvRecordTime: TextView = itemView.findViewById(R.id.tvRecordTime)
        val tvRecordDuration: TextView = itemView.findViewById(R.id.tvRecordDuration)
        val tvRecordDistance: TextView = itemView.findViewById(R.id.tvRecordDistance)
        val tvRecordSpeed: TextView = itemView.findViewById(R.id.tvRecordSpeed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sport_record_item, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        
        // 加载轨迹缩略图
        if (!record.trackThumbPath.isNullOrEmpty()) {
            val file = File(record.trackThumbPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(record.trackThumbPath)
                holder.imgTrackThumb.setImageBitmap(bitmap)
            } else {
                holder.imgTrackThumb.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else if (record.trackThumbRes != 0) {
            holder.imgTrackThumb.setImageResource(record.trackThumbRes)
        } else {
            holder.imgTrackThumb.setImageResource(R.drawable.ic_launcher_foreground)
        }
        
        holder.tvRecordTime.text = record.dateTime
        holder.tvRecordDuration.text = "用时：${record.duration}"
        holder.tvRecordDistance.text = "距离：${record.distance}"
        holder.tvRecordSpeed.text = "均速：${record.avgSpeed}"
    }

    override fun getItemCount(): Int = records.size
}

