package com.example.mushroomapp.domain

interface MushroomLocationRepository {

    fun getMushroom(): MushroomLocation

    fun getMushroomList(): List<MushroomLocation>
}