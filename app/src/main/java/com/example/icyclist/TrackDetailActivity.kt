package com.example.icyclist

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.PolylineOptions
import com.example.icyclist.community.CreatePostActivity
import com.example.icyclist.database.Converters
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.database.SportRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TrackDetailActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var aMap: AMap? = null
    private lateinit var sportDatabase: SportDatabase
    private var sportRecordId: Long = -1

    private lateinit var tvDateTime: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvAvgSpeed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_detail)

        sportRecordId = intent.getLongExtra("SPORT_RECORD_ID", -1)
        if (sportRecordId == -1L) {
            Toast.makeText(this, "无效的记录ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        aMap = mapView?.map

        tvDateTime = findViewById(R.id.tvDateTime)
        tvDistance = findViewById(R.id.tvDistance)
        tvDuration = findViewById(R.id.tvDuration)
        tvAvgSpeed = findViewById(R.id.tvAvgSpeed)

        sportDatabase = SportDatabase.getDatabase(this)
        loadTrackDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_track_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareTrack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareTrack() {
        aMap?.getMapScreenShot(object : AMap.OnMapScreenShotListener {
            override fun onMapScreenShot(bitmap: Bitmap?) {
                if (bitmap == null) {
                    Toast.makeText(this@TrackDetailActivity, "分享失败，无法获取地图截图", Toast.LENGTH_SHORT).show()
                    return
                }
                saveBitmapAndShare(bitmap)
            }

            override fun onMapScreenShot(bitmap: Bitmap?, status: Int) {
                // onMapScreenShot(Bitmap) will be called, so this can be ignored
            }
        })
    }

    private fun saveBitmapAndShare(bitmap: Bitmap) {
        lifecycleScope.launch {
            val imagePath = withContext(Dispatchers.IO) {
                saveBitmapToCache(bitmap)
            }
            if (imagePath != null) {
                val imageUri = Uri.fromFile(File(imagePath))
                val intent = Intent(this@TrackDetailActivity, CreatePostActivity::class.java).apply {
                    putExtra("image_uri", imageUri.toString())
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@TrackDetailActivity, "分享失败，无法保存地图截图", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): String? {
        val imageFileName = "track_share_${System.currentTimeMillis()}.png"
        val cacheDir = cacheDir
        val imageFile = File(cacheDir, imageFileName)
        try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return imageFile.absolutePath
        } catch (e: IOException) {
            Log.e("TrackDetailActivity", "Error saving bitmap", e)
            return null
        }
    }

    private fun loadTrackDetails() {
        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) {
                sportDatabase.sportRecordDao().getRecordById(sportRecordId)
            }
            if (record != null) {
                displaySportData(record)
                drawTrackOnMap(record.routePointsJson)
            } else {
                Toast.makeText(this@TrackDetailActivity, "无法加载运动记录", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySportData(record: SportRecordEntity) {
        tvDateTime.text = record.dateTime
        tvDistance.text = record.distance
        tvDuration.text = record.duration
        tvAvgSpeed.text = record.avgSpeed
    }

    private fun drawTrackOnMap(routePointsJson: String) {
        if (aMap == null) {
            Log.e("TrackDetailActivity", "AMap object is null, cannot draw track.")
            return
        }
        val converters = Converters()
        val routePoints = converters.toLatLngList(routePointsJson)

        if (routePoints.isNotEmpty()) {
            aMap?.addPolyline(
                PolylineOptions()
                    .addAll(routePoints)
                    .width(15f)
                    .color(Color.parseColor("#2196F3"))
            )

            // 移动镜头以显示完整轨迹
            val boundsBuilder = LatLngBounds.Builder()
            for (point in routePoints) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            aMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
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
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
