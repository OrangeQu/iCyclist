package com.example.icyclist.network.model

/**
 * 骑行记录
 */
data class RideRecord(
    val id: Long? = null,
    val userId: Long? = null,
    val startTime: String,  // ISO 8601 格式时间戳
    val endTime: String,
    val durationSeconds: Int,
    val distanceMeters: Double,
    val averageSpeedKmh: Double,
    val trackPoints: List<TrackPoint>,
    val title: String? = null,
    val createdAt: String? = null
)

/**
 * 轨迹点
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,  // ISO 8601 格式时间戳
    val speed: Double? = null,
    val altitude: Double? = null
)

