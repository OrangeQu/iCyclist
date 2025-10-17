package com.example.icyclist

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.database.SportRecordEntity
import com.example.icyclist.utils.TrackThumbnailGenerator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*
import androidx.core.content.ContextCompat

class SportTrackingActivity : AppCompatActivity(), AMapLocationListener, LocationSource {
    
    // 地图相关
    private var mapView: MapView? = null
    private var aMap: AMap? = null
    private var locationClient: AMapLocationClient? = null
    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    
    // UI控件
    private lateinit var tvTimer: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvAvgSpeed: TextView
    private lateinit var btnPause: FloatingActionButton
    private lateinit var btnStop: FloatingActionButton
    private lateinit var toolbar: Toolbar

    // 运动数据
    private var sportStartTime: Long = 0
    private var pausedTime: Long = 0
    private var totalPausedDuration: Long = 0
    private var isPaused = false
    private var currentRoutePoints = mutableListOf<LatLng>()
    private var currentPolyline: Polyline? = null
    private var totalDistance = 0.0 // 米
    private var maxSpeed = 0.0 // km/h
    private val speedList = mutableListOf<Double>()
    private var lastRecordedLocation: LatLng? = null // 最后记录的位置
    
    // 定时器
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    // 数据库
    private lateinit var sportDatabase: SportDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_tracking)
        
        // 初始化数据库
        sportDatabase = SportDatabase.getDatabase(this)
        
        // 初始化视图
        initViews()
        
        // 初始化地图
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        aMap = mapView?.map
        
        if (aMap == null) {
            Log.e("SportTrackingActivity", "❌ AMap 对象初始化失败")
            Toast.makeText(this, "地图初始化失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 设置隐私合规
        setupAMapPrivacyCompliance()
        
        // 配置地图
        setupMap()
        
        // 初始化定位
        initLocation()
        
        // 开始运动
        startSportRecording()
        
        // 处理返回按钮
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmDialog()
            }
        })
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            showExitConfirmDialog()
        }

        tvTimer = findViewById(R.id.tvTimer)

        val layoutDistance = findViewById<View>(R.id.layoutDistance)
        tvDistance = layoutDistance.findViewById(R.id.tvValue)
        layoutDistance.findViewById<TextView>(R.id.tvLabel).text = "距离(km)"

        val layoutSpeed = findViewById<View>(R.id.layoutSpeed)
        tvSpeed = layoutSpeed.findViewById(R.id.tvValue)
        layoutSpeed.findViewById<TextView>(R.id.tvLabel).text = "速度(km/h)"

        val layoutAvgSpeed = findViewById<View>(R.id.layoutAvgSpeed)
        tvAvgSpeed = layoutAvgSpeed.findViewById(R.id.tvValue)
        layoutAvgSpeed.findViewById<TextView>(R.id.tvLabel).text = "均速(km/h)"


        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)

        // 暂停/继续按钮
        btnPause.setOnClickListener {
            togglePause()
        }
        
        // 停止按钮 - 长按
        btnStop.setOnLongClickListener {
            showStopConfirmDialog()
            true
        }
        btnStop.setOnClickListener {
            Toast.makeText(this, "请长按结束运动", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupAMapPrivacyCompliance() {
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
            Log.d("SportTrackingActivity", "隐私合规设置成功")
        } catch (e: Exception) {
            Log.e("SportTrackingActivity", "隐私合规设置失败", e)
        }
    }
    
    private fun setupMap() {
        aMap?.let { map ->
            // 设置定位监听
            map.setLocationSource(this)
            map.isMyLocationEnabled = true
            
            // 配置定位样式
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            map.myLocationStyle = myLocationStyle
            
            // 设置地图UI
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            
            Log.d("SportTrackingActivity", "✅ 地图配置成功")
        }
    }
    
    private fun initLocation() {
        try {
            locationClient = AMapLocationClient(applicationContext)
            
            val locationOption = AMapLocationClientOption().apply {
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                interval = 2000
                isNeedAddress = false
                isMockEnable = false
                httpTimeOut = 20000
                isLocationCacheEnable = false
                isWifiActiveScan = true
                isOnceLocation = false
                isOnceLocationLatest = false
            }
            
            locationClient?.setLocationOption(locationOption)
            locationClient?.setLocationListener(this)
            locationClient?.startLocation()
            
            Log.d("SportTrackingActivity", "定位客户端初始化成功")
        } catch (e: Exception) {
            Log.e("SportTrackingActivity", "定位初始化失败", e)
            Toast.makeText(this, "定位初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        onLocationChangedListener = listener
        Log.d("SportTrackingActivity", "✅ LocationSource activated")
    }
    
    override fun deactivate() {
        onLocationChangedListener = null
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        Log.d("SportTrackingActivity", "LocationSource deactivated")
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == 0) {
            // 传递给地图
            onLocationChangedListener?.onLocationChanged(amapLocation)
            
            // 如果未暂停，记录轨迹点
            if (!isPaused) {
                recordLocationPoint(amapLocation)
            }
            
            // 更新速度显示
            val currentSpeed = amapLocation.speed * 3.6 // 转换为km/h
            updateSpeed(currentSpeed)
            
        } else {
            Log.e("SportTrackingActivity", "❌ 定位失败: ${amapLocation?.errorCode}")
        }
    }
    
    private fun startSportRecording() {
        sportStartTime = System.currentTimeMillis()
        totalPausedDuration = 0
        currentRoutePoints.clear()
        totalDistance = 0.0
        maxSpeed = 0.0
        speedList.clear()
        isPaused = false
        
        // 启动计时器
        startTimer()
        
        // 缩放地图到合理级别(18级,适合骑行)
        lastRecordedLocation?.let { location ->
            aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
            Log.d("SportTrackingActivity", "地图已缩放至18级")
        }
        
        Toast.makeText(this, "开始运动记录", Toast.LENGTH_SHORT).show()
        Log.d("SportTrackingActivity", "运动记录开始")
    }
    
    private fun togglePause() {
        if (isPaused) {
            // 继续
            totalPausedDuration += System.currentTimeMillis() - pausedTime
            isPaused = false
            btnPause.setImageResource(R.drawable.ic_baseline_pause_24)
            btnPause.backgroundTintList = ContextCompat.getColorStateList(this, R.color.purple_500)
            startTimer()
            Toast.makeText(this, "继续运动", Toast.LENGTH_SHORT).show()
        } else {
            // 暂停
            pausedTime = System.currentTimeMillis()
            isPaused = true
            btnPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            btnPause.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
            stopTimer()
            Toast.makeText(this, "已暂停", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (!isPaused) {
                    updateTimer()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
    
    private fun updateTimer() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - sportStartTime - totalPausedDuration
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60))
        
        tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    private fun updateSpeed(currentSpeed: Double) {
        tvSpeed.text = String.format("%.1f", currentSpeed)
    }
    
    private fun recordLocationPoint(location: AMapLocation) {
        // 精度过滤
        if (location.accuracy > 50) {
            Log.d("SportTrackingActivity", "精度较差(${location.accuracy}m)，忽略该点")
            return
        }
        
        val currentPoint = LatLng(location.latitude, location.longitude)
        currentRoutePoints.add(currentPoint)
        
        // 保存最后记录的位置
        lastRecordedLocation = currentPoint
        
        // 计算距离
        if (currentRoutePoints.size > 1) {
            val prevPoint = currentRoutePoints[currentRoutePoints.size - 2]
            val distance = calculateDistance(prevPoint, currentPoint)
            
            // 过滤异常距离
            if (distance < 100) {
                totalDistance += distance
                updateDistance()
            }
        }
        
        // 记录速度
        val currentSpeed = location.speed * 3.6
        if (currentSpeed > 0) {
            speedList.add(currentSpeed)
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed
            }
            updateAvgSpeed()
        }
        
        // 绘制轨迹
        drawTrack()
        
        // 移动相机跟随
        aMap?.animateCamera(CameraUpdateFactory.newLatLng(currentPoint))
        
        Log.d("SportTrackingActivity", "记录点: ${currentRoutePoints.size}, 距离: ${String.format("%.2f", totalDistance)}m")
    }
    
    private fun updateDistance() {
        tvDistance.text = String.format("%.2f", totalDistance / 1000.0)
    }
    
    private fun updateAvgSpeed() {
        val avgSpeed = if (speedList.isNotEmpty()) {
            speedList.average()
        } else {
            0.0
        }
        tvAvgSpeed.text = String.format("%.1f", avgSpeed)
    }
    
    private fun drawTrack() {
        if (currentRoutePoints.size < 2) return
        
        currentPolyline?.remove()
        currentPolyline = aMap?.addPolyline(
            PolylineOptions()
                .addAll(currentRoutePoints)
                .width(10f)
                .color(Color.parseColor("#2196F3"))
        )
    }
    
    private fun showStopConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("结束运动")
            .setMessage("确定要结束运动吗？运动数据将被保存。")
            .setPositiveButton("确定") { _, _ ->
                stopSportRecording()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showExitConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出运动")
            .setMessage("运动正在进行中，确定要退出吗？\n退出后运动数据将被丢弃。")
            .setPositiveButton("继续运动", null)
            .setNegativeButton("放弃并退出") { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun stopSportRecording() {
        stopTimer()
        
        if (currentRoutePoints.size < 2) {
            Toast.makeText(this, "轨迹点太少，无法保存", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - sportStartTime - totalPausedDuration
        
        // 计算平均速度
        val avgSpeed = if (duration > 0 && totalDistance > 0) {
            (totalDistance / 1000.0) / (duration / 3600000.0)
        } else {
            0.0
        }
        
        // 估算卡路里
        val calories = ((totalDistance / 1000.0) * 70 * 0.5).toInt()
        
        Toast.makeText(this, "正在保存运动记录...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // 生成轨迹缩略图
                val thumbnailPath = withContext(Dispatchers.IO) {
                    TrackThumbnailGenerator.generateThumbnail(
                        this@SportTrackingActivity,
                        currentRoutePoints.toList()
                    )
                }
                
                // 格式化数据
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dateTime = dateFormat.format(Date(sportStartTime))
                
                val durationStr = formatDuration(duration)
                val distanceStr = String.format("%.2f km", totalDistance / 1000.0)
                val avgSpeedStr = String.format("%.1f km/h", avgSpeed)
                
                // 创建数据库实体
                val converters = com.example.icyclist.database.Converters()
                val entity = SportRecordEntity(
                    dateTime = dateTime,
                    duration = durationStr,
                    distance = distanceStr,
                    avgSpeed = avgSpeedStr,
                    trackThumbPath = thumbnailPath,
                    routePointsJson = converters.fromLatLngList(currentRoutePoints),
                    startTime = sportStartTime,
                    endTime = endTime,
                    totalDistanceMeters = totalDistance,
                    maxSpeed = maxSpeed,
                    calories = calories
                )
                
                // 保存到本地数据库
                val recordId = withContext(Dispatchers.IO) {
                    sportDatabase.sportRecordDao().insertRecord(entity)
                }
                
                // 同时上传到服务器
                uploadRecordToServer(entity, recordId.toInt())
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SportTrackingActivity, "运动记录已保存", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
                Log.d("SportTrackingActivity", "运动记录已保存: $distanceStr, $durationStr")
                
            } catch (e: Exception) {
                Log.e("SportTrackingActivity", "保存运动记录失败", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SportTrackingActivity, "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
    
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLng = Math.toRadians(point2.longitude - point1.longitude)
        
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * 上传骑行记录到服务器
     */
    private fun uploadRecordToServer(entity: SportRecordEntity, recordId: Int) {
        lifecycleScope.launch {
            try {
                // 将轨迹点转换为服务器格式
                val trackPoints = currentRoutePoints.map { point ->
                    com.example.icyclist.network.model.TrackPoint(
                        latitude = point.latitude,
                        longitude = point.longitude,
                        timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                            .format(java.util.Date()),
                        speed = null,
                        altitude = null
                    )
                }
                
                // 创建骑行记录请求对象
                val rideRecord = com.example.icyclist.network.model.RideRecord(
                    startTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(entity.startTime)),
                    endTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(entity.endTime)),
                    durationSeconds = ((entity.endTime - entity.startTime) / 1000).toInt(),
                    distanceMeters = entity.totalDistanceMeters,
                    averageSpeedKmh = entity.avgSpeed.replace(" km/h", "").toDoubleOrNull() ?: 0.0,
                    trackPoints = trackPoints,
                    title = "骑行记录 - ${entity.dateTime}"
                )
                
                // 上传到服务器
                val apiService = com.example.icyclist.network.RetrofitClient.getApiService(this@SportTrackingActivity)
                val response = apiService.createRideRecord(rideRecord)
                
                if (response.isSuccessful) {
                    android.util.Log.d("SportTrackingActivity", "✅ 骑行记录已上传到服务器")
                } else {
                    android.util.Log.w("SportTrackingActivity", "⚠️ 上传失败，但本地已保存: ${response.code()}")
                }
            } catch (e: Exception) {
                // 上传失败不影响本地保存，只记录日志
                android.util.Log.e("SportTrackingActivity", "⚠️ 上传到服务器失败（本地已保存）: ${e.message}", e)
            }
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        stopTimer()
        
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        
        mapView?.onDestroy()
        mapView = null
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
