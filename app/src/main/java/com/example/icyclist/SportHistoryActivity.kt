package com.example.icyclist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.icyclist.adapter.SportRecordAdapter
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.utils.SportRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SportHistoryActivity : AppCompatActivity() {

    private lateinit var rvSportRecords: RecyclerView
    private lateinit var sportRecordAdapter: SportRecordAdapter
    private val sportRecords = mutableListOf<SportRecord>()
    private lateinit var sportDatabase: SportDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_history)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        sportDatabase = SportDatabase.getDatabase(this)
        rvSportRecords = findViewById(R.id.rvSportRecords)
        setupRecyclerView()
        loadAllSportRecords()
    }

    private fun setupRecyclerView() {
        rvSportRecords.layoutManager = LinearLayoutManager(this)
        sportRecordAdapter = SportRecordAdapter(
            sportRecords,
            onDelete = { /* 删除逻辑可以之后添加，或者在这里实现 */ },
            onShare = { /* 分享逻辑可以之后添加，或者在这里实现 */ },
            onItemClick = { record ->
                val intent = Intent(this, TrackDetailActivity::class.java).apply {
                    putExtra("SPORT_RECORD_ID", record.id)
                }
                startActivity(intent)
            }
        )
        rvSportRecords.adapter = sportRecordAdapter
    }

    private fun loadAllSportRecords() {
        lifecycleScope.launch {
            val entities = withContext(Dispatchers.IO) {
                sportDatabase.sportRecordDao().getAllRecords()
            }
            sportRecords.clear()
            sportRecords.addAll(entities.map { entity ->
                SportRecord(
                    id = entity.id,
                    dateTime = entity.dateTime,
                    duration = entity.duration,
                    distance = entity.distance,
                    avgSpeed = entity.avgSpeed,
                    trackThumbPath = entity.trackThumbPath,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    totalDistanceMeters = entity.totalDistanceMeters,
                    maxSpeed = entity.maxSpeed,
                    calories = entity.calories
                )
            })
            sportRecordAdapter.updateRecords(sportRecords)
        }
    }
}
