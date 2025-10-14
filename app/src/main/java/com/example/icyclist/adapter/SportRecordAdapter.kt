package com.example.icyclist.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.R
import com.example.icyclist.utils.SportRecord
import java.io.File

class SportRecordAdapter(
    private var records: List<SportRecord>,
    private val onDelete: (SportRecord) -> Unit,
    private val onShare: (SportRecord) -> Unit
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

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("记录操作")
                .setItems(arrayOf("分享至社区", "删除记录")) { dialog, which ->
                    when (which) {
                        0 -> onShare(record)
                        1 -> onDelete(record)
                    }
                }
                .setNegativeButton("取消", null)
                .show()
            true
        }
        
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

    fun updateRecords(newRecords: List<SportRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}

