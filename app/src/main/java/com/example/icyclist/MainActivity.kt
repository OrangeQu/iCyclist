package com.example.icyclist

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
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

class MainActivity : AppCompatActivity(), AMapLocationListener, LocationSource {
    // 地图控件引用
    private var mMapView: MapView? = null
    private var aMap: AMap? = null
    
    // 定位相关
    private var locationClient: AMapLocationClient? = null
    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    
    // UI控件
    private var tvLocation: TextView? = null
    private var tvAddress: TextView? = null
    private var fabLocation: FloatingActionButton? = null
    private var rvSportRecords: RecyclerView? = null
    private var btnStartSport: Button? = null
    private var btnStopSport: Button? = null
    
    // 定位状态
    private var isFirstLocation = true
    private var lastLocation: AMapLocation? = null
    
    // 运动记录相关
    private var sportRecordAdapter: SportRecordAdapter? = null
    private var sportRecords: MutableList<SportRecord> = mutableListOf()
    
    // 运动状态管理
    private var isSportRunning = false
    private var sportStartTime: Long = 0
    private var currentRoutePoints = mutableListOf<LatLng>()
    private var currentPolyline: Polyline? = null
    private var totalDistance = 0.0 // 米
    private var maxSpeed = 0.0 // km/h
    private val speedList = mutableListOf<Double>()
    
    // 数据库
    private lateinit var sportDatabase: SportDatabase

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化数据库
        sportDatabase = SportDatabase.getDatabase(this)

        // --- 第1步：初始化 MapView ---
        // 获取地图控件引用
        mMapView = findViewById<MapView>(R.id.map)
        // 必须调用 MapView 的生命周期方法
        mMapView?.onCreate(savedInstanceState)

        // --- 第2步：获取 AMap 地图对象 (核心修正) ---
        // ✅ 这是唯一正确的获取方式，使用官方提供的 .map 属性
        aMap = mMapView?.map
        
        // 添加一个健壮性检查，如果 aMap 仍然为空，则直接返回，避免后续空指针
        if (aMap == null) {
            Log.e("MainActivity", "❌ 关键错误：aMap 对象未能初始化！请检查布局文件、SDK密钥和依赖。")
            return
        }
        
        Log.d("MainActivity", "✅ AMap 对象初始化成功")

        // --- 第3步：执行其他初始化 ---
        initViews() // 初始化其他视图
        setupAMapPrivacyCompliance() // 设置隐私合规
        loadSportRecords() // 加载运动记录
        
        // --- 第4步：配置地图并开始定位 ---
        setupMap() // 配置地图（现在 aMap 肯定不是 null 了）

        // 检查权限并初始化定位
        if (checkPermissions()) {
            initLocation()
        } else {
            requestPermissions()
        }
    }

    private fun initViews() {
        tvLocation = findViewById(R.id.tvLocation)
        tvAddress = findViewById(R.id.tvAddress)
        fabLocation = findViewById(R.id.fabLocation)
        rvSportRecords = findViewById(R.id.rvSportRecords)
        btnStartSport = findViewById(R.id.btnStartSport)
        btnStopSport = findViewById(R.id.btnStopSport)
        
        // 设置定位按钮点击事件
        fabLocation?.setOnClickListener {
            relocateToCurrentPosition()
        }
        
        // 开始运动按钮
        btnStartSport?.setOnClickListener {
            startSportRecording()
        }
        
        // 结束运动按钮 - 长按事件
        btnStopSport?.setOnLongClickListener {
            stopSportRecording()
            true
        }
        
        // RecyclerView设置
        rvSportRecords?.layoutManager = LinearLayoutManager(this)
        sportRecordAdapter = SportRecordAdapter(sportRecords)
        rvSportRecords?.adapter = sportRecordAdapter

        // 滑动监听，滑动到底部自动隐藏地图
        rvSportRecords?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val firstVisible = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                if (dy > 10 && mMapView?.visibility == View.VISIBLE) {
                    // 向下滑动，隐藏地图
                    mMapView?.visibility = View.GONE
                } else if (dy < -10 && mMapView?.visibility == View.GONE && firstVisible == 0) {
                    // 向上滑动且回到顶部，显示地图
                    mMapView?.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupAMapPrivacyCompliance() {
        try {
            // 统一调用 ServiceSettings.updatePrivacyShow 和 updatePrivacyAgree
            // 第一个参数是Context，第二个是是否同意隐私政策，第三个是是否显示隐私政策窗口
//            ServiceSettings.updatePrivacyShow(this, true, true)
//            ServiceSettings.updatePrivacyAgree(this, true)
            
            // 同时，也为定位服务设置合规
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)

            Log.d("MainActivity", "隐私合规设置成功")
        } catch (e: Exception) {
            Log.e("MainActivity", "隐私合规设置失败", e)
        }
    }

    private fun setupMap() {
        // 配置地图（此时 aMap 应该已经在 onCreate 中初始化完成）
        if (aMap == null) {
            Log.e("MainActivity", "❌ setupMap: aMap 对象为空，无法配置地图")
            return
        }
        configureMap()
    }
    
    private fun configureMap() {
        // 配置地图设置
        
        // 显示地图
        aMap?.let { map ->
            // 第一步：设置定位监听
            map.setLocationSource(this)
            // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 在 Kotlin 中，更推荐直接使用属性赋值
            map.setMyLocationEnabled(true)

            // 第二步：配置并设置定位小蓝点的样式
            try {
                val myLocationStyle = MyLocationStyle()

                // 关键改动：通过 MyLocationStyle 来设置定位模式，而不是通过 AMap 对象。
                // 将原来的 map.setMyLocationType(AMap.LOCATION_TYPE_LOCATE) 这行代码的逻辑移到这里。
                // 常量也从 AMap.LOCATION_TYPE_LOCATE 变更为 MyLocationStyle.LOCATION_TYPE_LOCATE。
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)

                // (可选) 你还可以继续设置其他的样式，例如：
                // myLocationStyle.interval(2000) // 设置连续定位模式下的定位间隔，单位为毫秒。
                // myLocationStyle.strokeColor(Color.TRANSPARENT) // 设置圆形区域的边框颜色
                // myLocationStyle.radiusFillColor(Color.TRANSPARENT) // 设置圆形区域的填充颜色

                // 将配置好的 MyLocationStyle 应用到地图上
                map.myLocationStyle = myLocationStyle

                Log.d("MainActivity", "地图显示成功，已启用定位蓝点并设置样式")

            } catch (e: Exception) {
                Log.e("MainActivity", "配置定位样式失败: ${e.message}")
            }
        } ?: Log.w("MainActivity", "地图对象为空，无法显示")
    }

    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    initLocation()
                    Toast.makeText(this, getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_LONG).show()
                    updateLocationInfo(getString(R.string.permission_denied), "")
                }
            }
        }
    }

    private fun initLocation() {
        if (!checkPermissions()) {
            return
        }

        try {
            // 使用getApplicationContext()获取全进程有效的context
            locationClient = AMapLocationClient(applicationContext)

            // 创建AMapLocationClientOption对象
            val locationOption = AMapLocationClientOption().apply {
                // 设置定位场景（骑行相关应用，使用运动场景）
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
                
                // 设置高精度定位模式
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                
                // 设置连续定位间隔（毫秒），最低1000ms
                interval = 2000
                
                // 设置是否返回地址信息（默认返回地址信息）
                isNeedAddress = true
                
                // 设置是否允许模拟位置，默认为true
                isMockEnable = false
                
                // 设置定位请求超时时间（毫秒），建议不低于8000毫秒
                httpTimeOut = 20000
                
                // 关闭缓存机制以获取最新位置
                isLocationCacheEnable = false
                
                // 设置是否主动刷新WIFI，默认为false
                isWifiActiveScan = true
                
                // 设置为连续定位模式
                isOnceLocation = false
                isOnceLocationLatest = false
            }

            // 设置定位参数
            locationClient?.setLocationOption(locationOption)
            // 设置定位回调监听
            locationClient?.setLocationListener(this)
            // 启动定位
            locationClient?.startLocation()

            updateLocationInfo(getString(R.string.getting_location), "")
            Log.d("MainActivity", "定位客户端初始化成功")

        } catch (e: Exception) {
            Log.e("MainActivity", "Location init failed", e)
            updateLocationInfo(getString(R.string.location_init_failed), e.message ?: "")
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        // 1. 确保 amapLocation 对象不为空
        if (amapLocation != null) {
            // 2. 检查定位是否成功（errorCode 为 0 表示成功）
            if (amapLocation.errorCode == 0) {
                
                // 3. 检查 onLocationChangedListener 是否已初始化 (这是关键中的关键!)
                //    onLocationChangedListener 是在 LocationSource 的 activate 方法中被赋值的
                if (onLocationChangedListener != null) {
                    // 核心步骤：将定位结果 AMapLocation 对象传递给地图层。
                    // 只有执行了此方法，小蓝点才会显示在地图上。
                    onLocationChangedListener?.onLocationChanged(amapLocation)
                    Log.d("MainActivity", "✅ 定位成功，已将数据传递给地图层 -> Lat:${amapLocation.latitude}, Lon:${amapLocation.longitude}")

                    // 如果是第一次定位，将地图视图移动到当前定位点，并设置缩放级别
                    // 这样用户就能立刻看到自己的位置
                    if (isFirstLocation) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(amapLocation.latitude, amapLocation.longitude), 
                            16f
                        ))
                        isFirstLocation = false
                        Log.d("MainActivity", "📍 首次定位，已将地图移动到当前位置")
                    }

                } else {
                    Log.e("MainActivity", "❌ onLocationChanged 错误: onLocationChangedListener 为空！请检查 activate 方法是否被正确调用和赋值。")
                }
                
                // 如果运动正在进行，记录轨迹点
                if (isSportRunning) {
                    recordLocationPoint(amapLocation)
                }
                
                // 解析AMapLocation对象获取相应内容
                val latitude = amapLocation.latitude // 获取纬度
                val longitude = amapLocation.longitude // 获取经度
                val accuracy = amapLocation.accuracy // 获取精度信息
                val address = amapLocation.address // 地址信息
                val speed = amapLocation.speed // 速度（米/秒）
                
                // 获取基本地址信息
                val province = amapLocation.province // 省信息
                val city = amapLocation.city // 城市信息
                val district = amapLocation.district // 区域信息
                val street = amapLocation.street // 街道信息
                val streetNum = amapLocation.streetNum // 街道门牌号信息
                val aoiName = amapLocation.aoiName // AOI信息
                
                // 更新UI显示
                val speedText = if (speed > 0) "\n速度: ${String.format("%.1f", speed * 3.6)} km/h" else ""
                val locationText = "经度: ${String.format("%.6f", longitude)}\n纬度: ${String.format("%.6f", latitude)}$speedText\n精度: ${String.format("%.1f", accuracy)}m"
                
                // 获取详细地址信息
                val addressText = if (address.isNotEmpty()) {
                    address
                } else {
                    // 构建地址信息
                    buildString {
                        if (province.isNotEmpty()) append(province)
                        if (city.isNotEmpty()) append(city)
                        if (district.isNotEmpty()) append(district)
                        if (street.isNotEmpty()) append(street)
                        if (streetNum.isNotEmpty()) append(streetNum)
                        if (aoiName.isNotEmpty()) append(" ($aoiName)")
                    }.ifEmpty { getString(R.string.resolving_address) }
                }
                
                updateLocationInfo(locationText, addressText)
                
                // 保存当前位置作为上一个位置
                lastLocation = amapLocation

            } else {
                // 如果定位失败，打印错误信息，帮助调试
                Log.e("MainActivity", "❌ 定位失败, errCode: ${amapLocation.errorCode}, errInfo: ${amapLocation.errorInfo}")
                updateLocationInfo("${getString(R.string.location_failed)}: ${amapLocation.errorCode}", amapLocation.errorInfo)
            }
        } else {
            Log.e("MainActivity", "❌ onLocationChanged 错误: 回调的 amapLocation 对象为空")
            updateLocationInfo(getString(R.string.location_failed), "定位结果为空")
        }
    }

    private fun updateLocationInfo(locationText: String, addressText: String) {
        runOnUiThread {
            tvLocation?.text = locationText
            tvAddress?.text = addressText
        }
    }

    private fun relocateToCurrentPosition() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        // 重启定位服务
        locationClient?.stopLocation()
        locationClient?.startLocation()

        updateLocationInfo(getString(R.string.relocating), "")
        Toast.makeText(this, getString(R.string.relocating), Toast.LENGTH_SHORT).show()
        
        Log.d("MainActivity", "重新定位请求已发送")
    }

    // LocationSource 接口实现
    // 第二步：激活定位
    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        // 关键步骤：保存地图传递过来的监听器
        onLocationChangedListener = listener
        
        if (listener != null) {
            Log.d("MainActivity", "✅ activate 方法被调用，onLocationChangedListener 已成功赋值")
        } else {
            Log.e("MainActivity", "⚠️ activate 方法被调用，但传入的 listener 为空")
        }
        
        if (locationClient == null) {
            try {
                // 初始化定位
                locationClient = AMapLocationClient(applicationContext)
                
                // 初始化定位参数
                val mLocationOption = AMapLocationClientOption().apply {
                    // 设置为高精度定位模式
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    // 设置定位间隔（最小间隔支持为2000ms）
                    interval = 2000
                    // 设置是否返回地址信息
                    isNeedAddress = true
                    // 设置是否允许模拟位置
                    isMockEnable = false
                    // 设置定位请求超时时间
                    httpTimeOut = 20000
                    // 关闭缓存机制以获取最新位置
                    isLocationCacheEnable = false
                    // 设置是否主动刷新WIFI
                    isWifiActiveScan = true
                    // 设置为连续定位模式
                    isOnceLocation = false
                    isOnceLocationLatest = false
                }
                
                // 设置定位回调监听
                locationClient?.setLocationListener(this)
                // 设置定位参数
                locationClient?.setLocationOption(mLocationOption)
                // 此方法为每隔固定时间会发起一次定位请求
                // 启动定位
                locationClient?.startLocation()
                
                Log.d("MainActivity", "🚀 定位服务已在activate方法中启动")
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ 激活定位服务失败", e)
            }
        } else {
            Log.d("MainActivity", "ℹ️ 定位客户端已存在，无需重新初始化")
        }
    }


    // 停止定位
    override fun deactivate() {
        onLocationChangedListener = null
        
        if (locationClient != null) {
            // 停止定位
            locationClient?.stopLocation()
            // 销毁定位客户端
            locationClient?.onDestroy()
            locationClient = null
            Log.d("MainActivity", "定位服务已在deactivate方法中停止")
        }
    }

    override fun onResume() {
        super.onResume()
        // 在activity执行onResume时执行mMapView.onResume()，重新绘制加载地图
        mMapView?.onResume()
        
        // 如果 aMap 为空，尝试重新获取（正常情况下不应该发生）
        if (aMap == null) {
            aMap = mMapView?.map
            if (aMap != null) {
                Log.d("MainActivity", "✅ onResume: 重新获取 AMap 对象成功")
                setupMap()
            } else {
                Log.e("MainActivity", "❌ onResume: 无法获取 AMap 对象")
            }
        }
        
        // 如果定位客户端为空，重新初始化
        if (locationClient == null && checkPermissions()) {
            initLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        // 在activity执行onPause时执行mMapView.onPause()，暂停地图的绘制
        mMapView?.onPause()
        
        // 暂停定位
        locationClient?.stopLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 停止并销毁定位客户端
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView?.onDestroy()
        mMapView = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState(outState)，保存地图当前的状态
        mMapView?.onSaveInstanceState(outState)
    }
    
    // ==================== 运动记录功能 ====================
    
    /**
     * 开始运动记录
     */
    private fun startSportRecording() {
        if (isSportRunning) {
            Toast.makeText(this, "运动记录已在进行中", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!checkPermissions()) {
            Toast.makeText(this, "需要定位权限才能开始运动", Toast.LENGTH_SHORT).show()
            requestPermissions()
            return
        }
        
        // 初始化运动数据
        isSportRunning = true
        sportStartTime = System.currentTimeMillis()
        currentRoutePoints.clear()
        totalDistance = 0.0
        maxSpeed = 0.0
        speedList.clear()
        
        // 清除之前的轨迹线
        currentPolyline?.remove()
        currentPolyline = null
        
        // 更新UI
        btnStartSport?.visibility = View.GONE
        btnStopSport?.visibility = View.VISIBLE
        
        Toast.makeText(this, "开始运动记录", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "运动记录开始")
    }
    
    /**
     * 记录定位点
     */
    private fun recordLocationPoint(location: AMapLocation) {
        val currentPoint = LatLng(location.latitude, location.longitude)
        
        // 如果精度太差，忽略该点
        if (location.accuracy > 50) {
            Log.d("MainActivity", "精度较差(${location.accuracy}m)，忽略该点")
            return
        }
        
        currentRoutePoints.add(currentPoint)
        
        // 计算与上一个点的距离
        if (currentRoutePoints.size > 1) {
            val prevPoint = currentRoutePoints[currentRoutePoints.size - 2]
            val distance = calculateDistance(prevPoint, currentPoint)
            
            // 过滤掉异常的距离（可能是定位漂移）
            if (distance < 100) { // 两次定位间隔最多100米
                totalDistance += distance
            }
        }
        
        // 记录速度
        val currentSpeed = location.speed * 3.6 // 转换为km/h
        if (currentSpeed > 0) {
            speedList.add(currentSpeed)
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed
            }
        }
        
        // 在地图上绘制轨迹
        drawTrack()
        
        Log.d("MainActivity", "记录点: ${currentRoutePoints.size}, 距离: ${String.format("%.2f", totalDistance)}m")
    }
    
    /**
     * 在地图上绘制轨迹
     */
    private fun drawTrack() {
        if (currentRoutePoints.size < 2) return
        
        // 移除旧的轨迹线
        currentPolyline?.remove()
        
        // 绘制新的轨迹线
        currentPolyline = aMap?.addPolyline(
            PolylineOptions()
                .addAll(currentRoutePoints)
                .width(10f)
                .color(Color.parseColor("#2196F3"))
        )
    }
    
    /**
     * 停止运动记录
     */
    private fun stopSportRecording() {
        if (!isSportRunning) {
            Toast.makeText(this, "没有正在进行的运动", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (currentRoutePoints.size < 2) {
            Toast.makeText(this, "轨迹点太少，无法保存", Toast.LENGTH_SHORT).show()
            resetSportState()
            return
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - sportStartTime
        
        // 计算平均速度
        val avgSpeed = if (duration > 0 && totalDistance > 0) {
            (totalDistance / 1000.0) / (duration / 3600000.0) // km/h
        } else {
            0.0
        }
        
        // 估算卡路里（简单估算：按体重70kg，骑行消耗约0.5卡/kg/km）
        val calories = ((totalDistance / 1000.0) * 70 * 0.5).toInt()
        
        Toast.makeText(this, "正在保存运动记录...", Toast.LENGTH_SHORT).show()
        
        // 在后台保存记录
        lifecycleScope.launch {
            try {
                // 生成轨迹缩略图
                val thumbnailPath = withContext(Dispatchers.IO) {
                    TrackThumbnailGenerator.generateThumbnail(
                        this@MainActivity,
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
                
                // 保存到数据库
                withContext(Dispatchers.IO) {
                    sportDatabase.sportRecordDao().insertRecord(entity)
                }
                
                // 重新加载列表
                loadSportRecords()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "运动记录已保存", Toast.LENGTH_SHORT).show()
                    resetSportState()
                }
                
                Log.d("MainActivity", "运动记录已保存: $distanceStr, $durationStr")
                
            } catch (e: Exception) {
                Log.e("MainActivity", "保存运动记录失败", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 重置运动状态
     */
    private fun resetSportState() {
        isSportRunning = false
        currentRoutePoints.clear()
        totalDistance = 0.0
        maxSpeed = 0.0
        speedList.clear()
        sportStartTime = 0
        
        // 清除轨迹线
        currentPolyline?.remove()
        currentPolyline = null
        
        // 更新UI
        btnStartSport?.visibility = View.VISIBLE
        btnStopSport?.visibility = View.GONE
    }
    
    /**
     * 加载运动记录
     */
    private fun loadSportRecords() {
        lifecycleScope.launch {
            try {
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
                
                sportRecordAdapter?.notifyDataSetChanged()
                Log.d("MainActivity", "加载了 ${sportRecords.size} 条运动记录")
                
            } catch (e: Exception) {
                Log.e("MainActivity", "加载运动记录失败", e)
            }
        }
    }
    
    /**
     * 计算两点之间的距离（米）
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0 // 地球半径（米）
        
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
     * 格式化时长
     */
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
