package com.icyclist.server.model

import java.sql.Timestamp

data class RideRecord(
    var id: Long? = null,
    var userId: Long,
    var startTime: Timestamp,
    var endTime: Timestamp,
    var durationSeconds: Int,
    var distanceMeters: Double,
    var averageSpeedKmh: Double,
    var trackPoints: List<TrackPoint>,
    var title: String? = null,
    var createdAt: Timestamp? = null
)






















