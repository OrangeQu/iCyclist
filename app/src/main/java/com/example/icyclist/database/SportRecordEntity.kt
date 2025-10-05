package com.example.icyclist.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "sport_records")
@TypeConverters(Converters::class)
data class SportRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: String,
    val duration: String,
    val distance: String,
    val avgSpeed: String,
    val trackThumbPath: String?,
    val routePointsJson: String, // GPS轨迹点列表的JSON字符串
    val startTime: Long,
    val endTime: Long,
    val totalDistanceMeters: Double,
    val maxSpeed: Double,
    val calories: Int
)
