package com.example.icyclist

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.icyclist.database.Converters
import com.example.icyclist.database.SportDatabase
import com.example.icyclist.database.SportRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
