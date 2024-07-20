package com.example.mushroomapp.domain

import com.mapbox.geojson.Polygon

data class MushroomLocation(
    val areaId: Int,
    val areaName: String,
    val area: Int,
    val polygon: Polygon,
    val description: String,
    val address: String,
    val mushrooms: String,
    val phoneNumber: String,
    val email: String
)
