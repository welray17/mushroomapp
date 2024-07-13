package com.example.mushroomapp.domain

class GetMushroomListUseCase(private val mushroomLocationRepository: MushroomLocationRepository) {

    fun getMushroomList(): List<MushroomLocation> {
        return mushroomLocationRepository.getMushroomList()
    }
}