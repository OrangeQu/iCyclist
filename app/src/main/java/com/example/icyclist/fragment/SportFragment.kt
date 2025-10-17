package com.example.icyclist.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.example.icyclist.MainContainerActivity
import com.example.icyclist.R
import com.example.icyclist.SportHistoryActivity
import com.example.icyclist.SportTrackingActivity
import com.example.icyclist.adapter.SportRecordAdapter
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.manager.UserManager
import com.example.icyclist.utils.SportRecord
import com.example.icyclist.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.example.icyclist.TrackDetailActivity

class SportFragment : Fragment(), AMapLocationListener, LocationSource {
    
    // 地图控件引用
    private var mMapView: MapView? = null
    private var aMap: AMap? = null
    
    // 定位相关
    private var locationClient: AMapLocationClient? = null
    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    
    // UI控件
    private var toolbar: Toolbar? = null
    private var tvLocation: TextView? = null
    private var tvAddress: TextView? = null
    private var fabLocation: FloatingActionButton? = null
    private var rvSportRecords: RecyclerView? = null
    private var btnStartSport: MaterialButton? = null
    private var tvMoreHistory: TextView? = null
    
    // 定位状态
    private var isFirstLocation = true
    private var lastLocation: AMapLocation? = null
    
    // 运动记录相关
    private var sportRecordAdapter: SportRecordAdapter? = null
    private var sportRecords: MutableList<SportRecord> = mutableListOf()
    
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化数据库
        sportDatabase = SportDatabase.getDatabase(requireContext())

        // --- 第1步：初始化 MapView ---
        mMapView = view.findViewById(R.id.map)
        mMapView?.onCreate(savedInstanceState)

        // --- 第2步：获取 AMap 地图对象 ---
        aMap = mMapView?.map
        
        if (aMap == null) {
            Log.e("SportFragment", "❌ 关键错误：aMap 对象未能初始化！")
            return
        }
        
        Log.d("SportFragment", "✅ AMap 对象初始化成功")

        // --- 第3步：执行其他初始化 ---
        initViews(view)
        setupAMapPrivacyCompliance()
        loadSportRecords()
        
        // --- 第4步：配置地图并开始定位 ---
        setupMap()

        // 检查权限并初始化定位
        if (checkPermissions()) {
            initLocation()
        } else {
            requestPermissions()
        }
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvAddress = view.findViewById(R.id.tvAddress)
        fabLocation = view.findViewById(R.id.fabLocation)
        rvSportRecords = view.findViewById(R.id.rvSportRecords)
        btnStartSport = view.findViewById(R.id.btnStartSport)
        tvMoreHistory = view.findViewById(R.id.tvMoreHistory)
        
        // 设置定位按钮点击事件
        fabLocation?.setOnClickListener {
            relocateToCurrentPosition()
        }
        
        // 开始运动按钮 - 启动运动记录Activity
        btnStartSport?.setOnClickListener {
            if (!checkPermissions()) {
                Toast.makeText(requireContext(), "需要定位权限才能开始运动", Toast.LENGTH_SHORT).show()
                requestPermissions()
                return@setOnClickListener
            }
            
            val intent = Intent(requireContext(), SportTrackingActivity::class.java)
            startActivity(intent)
        }
        
        tvMoreHistory?.setOnClickListener {
            val intent = Intent(requireContext(), SportHistoryActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView设置
        rvSportRecords?.layoutManager = LinearLayoutManager(requireContext())
        sportRecordAdapter = SportRecordAdapter(
            sportRecords,
            onDelete = { record -> deleteSportRecord(record) },
            onShare = { record -> shareSportRecord(record) },
            onItemClick = { record ->
                val intent = Intent(requireContext(), TrackDetailActivity::class.java).apply {
                    putExtra("SPORT_RECORD_ID", record.id)
                }
                startActivity(intent)
            }
        )
        rvSportRecords?.adapter = sportRecordAdapter
    }

    private fun setupAMapPrivacyCompliance() {
        try {
            AMapLocationClient.updatePrivacyShow(requireContext(), true, true)
            AMapLocationClient.updatePrivacyAgree(requireContext(), true)
            Log.d("SportFragment", "隐私合规设置成功")
        } catch (e: Exception) {
            Log.e("SportFragment", "隐私合规设置失败", e)
        }
    }

    private fun setupMap() {
        if (aMap == null) {
            Log.e("SportFragment", "❌ setupMap: aMap 对象为空，无法配置地图")
            return
        }
        configureMap()
    }
    
    private fun configureMap() {
        aMap?.let { map ->
            map.setLocationSource(this)
            map.setMyLocationEnabled(true)

            try {
                val myLocationStyle = MyLocationStyle()
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
                map.myLocationStyle = myLocationStyle
                Log.d("SportFragment", "地图显示成功，已启用定位蓝点并设置样式")
            } catch (e: Exception) {
                Log.e("SportFragment", "配置定位样式失败: ${e.message}")
            }
        } ?: Log.w("SportFragment", "地图对象为空，无法显示")
    }

    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissions(REQUIRED_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
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
                    Toast.makeText(requireContext(), getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.location_permission_required), Toast.LENGTH_LONG).show()
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
            locationClient = AMapLocationClient(requireContext().applicationContext)

            val locationOption = AMapLocationClientOption().apply {
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                interval = 2000
                isNeedAddress = true
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

            updateLocationInfo(getString(R.string.getting_location), "")
            Log.d("SportFragment", "定位客户端初始化成功")

        } catch (e: Exception) {
            Log.e("SportFragment", "Location init failed", e)
            updateLocationInfo(getString(R.string.location_init_failed), e.message ?: "")
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null) {
            if (amapLocation.errorCode == 0) {
                
                if (onLocationChangedListener != null) {
                    onLocationChangedListener?.onLocationChanged(amapLocation)
                    Log.d("SportFragment", "✅ 定位成功 -> Lat:${amapLocation.latitude}, Lon:${amapLocation.longitude}")

                    if (isFirstLocation) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(amapLocation.latitude, amapLocation.longitude), 
                            16f
                        ))
                        isFirstLocation = false
                        Log.d("SportFragment", "📍 首次定位，已将地图移动到当前位置")
                    }

                } else {
                    Log.e("SportFragment", "❌ onLocationChanged 错误: onLocationChangedListener 为空！")
                }
                
                val latitude = amapLocation.latitude
                val longitude = amapLocation.longitude
                val accuracy = amapLocation.accuracy
                val address = amapLocation.address
                val speed = amapLocation.speed
                
                val province = amapLocation.province
                val city = amapLocation.city
                val district = amapLocation.district
                val street = amapLocation.street
                val streetNum = amapLocation.streetNum
                val aoiName = amapLocation.aoiName
                
                val speedText = if (speed > 0) "\n速度: ${String.format("%.1f", speed * 3.6)} km/h" else ""
                val locationText = "经度: ${String.format("%.6f", longitude)}\n纬度: ${String.format("%.6f", latitude)}$speedText\n精度: ${String.format("%.1f", accuracy)}m"
                
                val addressText = if (address.isNotEmpty()) {
                    address
                } else {
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
                lastLocation = amapLocation

            } else {
                Log.e("SportFragment", "❌ 定位失败, errCode: ${amapLocation.errorCode}, errInfo: ${amapLocation.errorInfo}")
                updateLocationInfo("${getString(R.string.location_failed)}: ${amapLocation.errorCode}", amapLocation.errorInfo)
            }
        } else {
            Log.e("SportFragment", "❌ onLocationChanged 错误: 回调的 amapLocation 对象为空")
            updateLocationInfo(getString(R.string.location_failed), "定位结果为空")
        }
    }

    private fun updateLocationInfo(locationText: String, addressText: String) {
        activity?.runOnUiThread {
            tvLocation?.text = locationText
            tvAddress?.text = addressText
        }
    }

    private fun relocateToCurrentPosition() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        locationClient?.stopLocation()
        locationClient?.startLocation()

        updateLocationInfo(getString(R.string.relocating), "")
        Toast.makeText(requireContext(), getString(R.string.relocating), Toast.LENGTH_SHORT).show()
        
        Log.d("SportFragment", "重新定位请求已发送")
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        onLocationChangedListener = listener
        
        if (listener != null) {
            Log.d("SportFragment", "✅ activate 方法被调用，onLocationChangedListener 已成功赋值")
        } else {
            Log.e("SportFragment", "⚠️ activate 方法被调用，但传入的 listener 为空")
        }
        
        if (locationClient == null) {
            try {
                locationClient = AMapLocationClient(requireContext().applicationContext)
                
                val mLocationOption = AMapLocationClientOption().apply {
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    interval = 2000
                    isNeedAddress = true
                    isMockEnable = false
                    httpTimeOut = 20000
                    isLocationCacheEnable = false
                    isWifiActiveScan = true
                    isOnceLocation = false
                    isOnceLocationLatest = false
                }
                
                locationClient?.setLocationListener(this)
                locationClient?.setLocationOption(mLocationOption)
                locationClient?.startLocation()
                
                Log.d("SportFragment", "🚀 定位服务已在activate方法中启动")
            } catch (e: Exception) {
                Log.e("SportFragment", "❌ 激活定位服务失败", e)
            }
        } else {
            Log.d("SportFragment", "ℹ️ 定位客户端已存在，无需重新初始化")
        }
    }

    override fun deactivate() {
        onLocationChangedListener = null
        
        if (locationClient != null) {
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null
            Log.d("SportFragment", "定位服务已在deactivate方法中停止")
        }
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
        loadSportRecords()
        
        if (aMap == null) {
            aMap = mMapView?.map
            if (aMap != null) {
                Log.d("SportFragment", "✅ onResume: 重新获取 AMap 对象成功")
                setupMap()
            } else {
                Log.e("SportFragment", "❌ onResume: 无法获取 AMap 对象")
            }
        }
        
        if (locationClient == null && checkPermissions()) {
            initLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
        locationClient?.stopLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        
        mMapView?.onDestroy()
        mMapView = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }
    
    private fun loadSportRecords() {
        lifecycleScope.launch {
            try {
                // 1. 优先从服务器获取
                val userId = UserManager.getUserId(requireContext())
                if (userId != null && userId > 0) {
                    val apiService = RetrofitClient.getApiService(requireContext())
                    val response = apiService.getUserRideRecords(userId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val serverRecords = response.body()!!
                        Log.d("SportFragment", "从服务器加载了 ${serverRecords.size} 条运动记录")
                        
                        // 转换为SportRecord并显示最新3条
                        sportRecords.clear()
                        sportRecords.addAll(serverRecords.take(3).map { rideRecord ->
                            SportRecord(
                                id = rideRecord.id ?: 0L,
                                dateTime = rideRecord.startTime,
                                duration = formatDuration((rideRecord.durationSeconds * 1000L)),
                                distance = formatDistance(rideRecord.distanceMeters / 1000.0), // 米转公里
                                avgSpeed = formatSpeed(rideRecord.averageSpeedKmh),
                                maxSpeed = rideRecord.averageSpeedKmh, // 使用平均速度作为最大速度
                                calories = ((rideRecord.distanceMeters / 1000.0) * 50).toInt(), // 估算卡路里
                                trackThumbPath = null, // 服务器记录暂不支持缩略图
                                startTime = System.currentTimeMillis(), // 使用当前时间作为占位
                                endTime = System.currentTimeMillis(),
                                totalDistanceMeters = rideRecord.distanceMeters
                            )
                        })
                        
                        (rvSportRecords?.adapter as? SportRecordAdapter)?.updateRecords(sportRecords)
                        Log.d("SportFragment", "显示了 ${sportRecords.size} 条运动记录")
                        return@launch
                    }
                }
                
                // 2. 网络失败或未登录，降级到本地缓存
                Log.d("SportFragment", "从服务器加载失败，使用本地缓存")
                loadFromLocalCache()
                
            } catch (e: Exception) {
                Log.e("SportFragment", "从服务器加载运动记录失败: ${e.message}", e)
                // 降级到本地缓存
                loadFromLocalCache()
            }
        }
    }
    
    /**
     * 格式化时长 (秒 -> HH:mm:ss)
     */
    private fun formatDuration(durationMillis: Long): String {
        val seconds = (durationMillis / 1000).toInt()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
    
    /**
     * 格式化距离 (米 -> XX.XX km)
     */
    private fun formatDistance(distanceKm: Double): String {
        return String.format("%.2f km", distanceKm)
    }
    
    /**
     * 格式化速度 (km/h -> XX.X km/h)
     */
    private fun formatSpeed(speed: Double): String {
        return String.format("%.1f km/h", speed)
    }
    
    /**
     * 从本地缓存加载运动记录（后备方案）
     */
    private suspend fun loadFromLocalCache() {
        try {
            val entities = withContext(Dispatchers.IO) {
                sportDatabase.sportRecordDao().getLatestRecords(3)
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
            
            (rvSportRecords?.adapter as? SportRecordAdapter)?.updateRecords(sportRecords)
            Log.d("SportFragment", "从本地缓存加载了 ${sportRecords.size} 条运动记录")
            
        } catch (e: Exception) {
            Log.e("SportFragment", "从本地缓存加载运动记录失败", e)
        }
    }

    private fun shareSportRecord(record: SportRecord) {
        lifecycleScope.launch {
            try {
                val userEmail = UserManager.getCurrentUserEmail(requireContext()) ?: "unknown"
                val userNickname = UserManager.getCurrentUserNickname(requireContext()) ?: "骑行者"
                val userAvatarPath = UserManager.getCurrentUserAvatar(requireContext())
                
                val content = "在 ${record.dateTime} 骑行了 ${record.distance}，用时 ${record.duration}，均速 ${record.avgSpeed}。一起加入我的骑行之旅吧！"
                
                Log.d("SportFragment", "准备分享: userEmail=$userEmail, nickname=$userNickname, thumbnail=${record.trackThumbPath}")
                
                // 保存到数据库
                val insertedId = withContext(Dispatchers.IO) {
                    val post = com.example.icyclist.database.CommunityPostEntity(
                        userNickname = userNickname,
                        userAvatar = userAvatarPath ?: "ic_twotone_person_24", // 使用用户头像，如果没有则用默认
                        content = content,
                        imageUrl = record.trackThumbPath, // 运动轨迹图作为帖子图片
                        sportRecordId = record.id,
                        sportDistance = record.distance,
                        sportDuration = record.duration,
                        sportThumbPath = record.trackThumbPath
                    )
                    sportDatabase.communityPostDao().insertPost(post)
                }
                
                Log.d("SportFragment", "✅ 分享成功! Post saved to local DB.")
                
                Toast.makeText(requireContext(), "已分享到社区", Toast.LENGTH_SHORT).show()
                
                // 切换到社区Fragment
                (activity as? MainContainerActivity)?.selectMenuItem(R.id.menu_community)
                
            } catch (e: Exception) {
                Log.e("SportFragment", "❌ 分享失败", e)
                Toast.makeText(requireContext(), "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteSportRecord(record: SportRecord) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    sportDatabase.sportRecordDao().deleteRecord(
                        sportDatabase.sportRecordDao().getRecordById(record.id)!!
                    )
                    if (!record.trackThumbPath.isNullOrEmpty()) {
                        val file = File(record.trackThumbPath)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
                loadSportRecords()
                Toast.makeText(requireContext(), "记录已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SportFragment", "删除记录失败", e)
                Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
