package com.example.mushroomapp.domain

import com.mapbox.geojson.Polygon

data class MushroomLocation(
    val mushroomId: Int,
    val mushroomName: String,
    val area: Int,
    val polygon: Polygon
)
