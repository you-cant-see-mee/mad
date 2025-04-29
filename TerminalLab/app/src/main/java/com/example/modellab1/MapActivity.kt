package com.example.modellab1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val LOCATION_PERMISSION_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("prefs", MODE_PRIVATE))
        setContentView(R.layout.activity_map)

        map = findViewById(R.id.mapView)
        map.setMultiTouchControls(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        } else {
            setupMap()
        }
    }

    private fun setupMap() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            val myLocation: GeoPoint? = locationOverlay.myLocation
            runOnUiThread {
                if (myLocation != null) {
                    val myPoint = GeoPoint(myLocation.latitude, myLocation.longitude)
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(myPoint)

                    addMockStoreMarkers(myPoint)
                } else {
                    Toast.makeText(this, "Couldn't fetch current location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addMockStoreMarkers(center: GeoPoint) {
        val stores = listOf(
            Triple("Apache Electronics", center.latitude + 0.001, center.longitude + 0.001),
            Triple("Jio store", center.latitude - 0.001, center.longitude + 0.001),
            Triple("HP world", center.latitude, center.longitude - 0.001)
        )

        for ((name, lat, lon) in stores) {
            val marker = Marker(map)
            marker.position = GeoPoint(lat, lon)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = name
            map.overlays.add(marker)
        }

        map.invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMap()
        } else {
            Toast.makeText(this, "Location permission required to show stores", Toast.LENGTH_SHORT).show()
        }
    }
}
