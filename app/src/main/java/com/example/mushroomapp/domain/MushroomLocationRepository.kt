package com.example.mushroomapp.domain

interface MushroomLocationRepository {

    fun getMushroom(id: Int): MushroomLocation

    fun getMushroomList(): List<MushroomLocation>
}