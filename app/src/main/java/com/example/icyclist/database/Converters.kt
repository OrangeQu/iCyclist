package com.example.icyclist.database

import androidx.room.TypeConverter
import com.amap.api.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLatLngList(value: List<LatLng>?): String {
        if (value == null) return "[]"
        val points = value.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }
        return gson.toJson(points)
    }

    @TypeConverter
    fun toLatLngList(value: String): List<LatLng> {
        if (value.isEmpty() || value == "[]") return emptyList()
        val type = object : TypeToken<List<Map<String, Double>>>() {}.type
        val points: List<Map<String, Double>> = gson.fromJson(value, type)
        return points.map { LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0) }
    }
}
