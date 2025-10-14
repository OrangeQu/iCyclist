package com.example.icyclist.utils

import com.amap.api.maps.model.LatLng

data class SportRecord(
    val id: Long = 0,
    val dateTime: String, // 记录时间
    val duration: String, // 用时 (格式: HH:mm:ss)
    val distance: String, // 距离 (格式: XX.XX km)
    val avgSpeed: String, // 均速 (格式: XX.X km/h)
    val trackThumbRes: Int = 0, // 轨迹缩略图资源id (用于示例数据)
    val trackThumbPath: String? = null, // 轨迹缩略图文件路径 (用于实际记录)
    val routePoints: List<LatLng> = emptyList(), // GPS轨迹点列表
    val startTime: Long = 0, // 开始时间戳
    val endTime: Long = 0, // 结束时间戳
    val totalDistanceMeters: Double = 0.0, // 总距离(米)
    val maxSpeed: Double = 0.0, // 最大速度(km/h)
    val calories: Int = 0 // 消耗卡路里
)
