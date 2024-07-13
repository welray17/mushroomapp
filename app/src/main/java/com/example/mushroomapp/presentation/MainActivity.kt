package com.example.mushroomapp.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mushroomapp.databinding.ActivityMainBinding
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapView = binding.mapView
        setUpMapView()

    }

    private fun setUpMapView() {
        mapView.mapboxMap.loadStyle(Style.OUTDOORS)
        val locale = resources.configuration.locale
        mapView.mapboxMap.getStyle { style ->
            style.localizeLabels(locale)
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}