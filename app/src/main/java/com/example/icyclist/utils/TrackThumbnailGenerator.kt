package com.example.icyclist.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.amap.api.maps.model.LatLng
import java.io.File
import java.io.FileOutputStream

object TrackThumbnailGenerator {
    
    /**
     * 生成轨迹缩略图
     * @param context 上下文
     * @param routePoints 路径点列表
     * @param width 缩略图宽度
     * @param height 缩略图高度
     * @return 缩略图文件路径
     */
    fun generateThumbnail(
        context: Context,
        routePoints: List<LatLng>,
        width: Int = 300,
        height: Int = 300
    ): String? {
        if (routePoints.isEmpty()) return null
        
        try {
            // 创建bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // 背景色
            canvas.drawColor(Color.parseColor("#F5F5F5"))
            
            // 计算经纬度边界
            var minLat = routePoints[0].latitude
            var maxLat = routePoints[0].latitude
            var minLng = routePoints[0].longitude
            var maxLng = routePoints[0].longitude
            
            for (point in routePoints) {
                if (point.latitude < minLat) minLat = point.latitude
                if (point.latitude > maxLat) maxLat = point.latitude
                if (point.longitude < minLng) minLng = point.longitude
                if (point.longitude > maxLng) maxLng = point.longitude
            }
            
            val latRange = maxLat - minLat
            val lngRange = maxLng - minLng
            
            // 添加边距
            val padding = 20f
            val drawWidth = width - 2 * padding
            val drawHeight = height - 2 * padding
            
            // 计算缩放比例（保持纵横比）
            val scale: Float = if (latRange / lngRange > drawHeight / drawWidth) {
                (drawHeight / latRange).toFloat()
            } else {
                (drawWidth / lngRange).toFloat()
            }
            
            // 将经纬度转换为画布坐标
            fun latLngToCanvas(point: LatLng): Pair<Float, Float> {
                val x = padding + (point.longitude - minLng).toFloat() * scale
                val y = height.toFloat() - padding - (point.latitude - minLat).toFloat() * scale
                return Pair(x, y)
            }
            
            // 绘制路径
            val pathPaint = Paint().apply {
                color = Color.parseColor("#2196F3")
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            
            val path = Path()
            val firstPoint = latLngToCanvas(routePoints[0])
            path.moveTo(firstPoint.first, firstPoint.second)
            
            for (i in 1 until routePoints.size) {
                val canvasPoint = latLngToCanvas(routePoints[i])
                path.lineTo(canvasPoint.first, canvasPoint.second)
            }
            
            canvas.drawPath(path, pathPaint)
            
            // 绘制起点
            val startPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(firstPoint.first, firstPoint.second, 8f, startPaint)
            
            // 绘制终点
            val endPaint = Paint().apply {
                color = Color.parseColor("#F44336")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val lastPoint = latLngToCanvas(routePoints.last())
            canvas.drawCircle(lastPoint.first, lastPoint.second, 8f, endPaint)
            
            // 保存到文件
            val fileName = "track_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            
            bitmap.recycle()
            
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 删除缩略图文件
     */
    fun deleteThumbnail(filePath: String?) {
        if (filePath.isNullOrEmpty()) return
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
