package com.example.mushroomapp.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mushroomapp.databinding.ActivityMainBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapView = binding.mapView
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(-98.0, 39.5))
                .pitch(0.0)
                .zoom(2.0)
                .bearing(0.0)
                .build()
        )
    }
}