package com.example.mushroomapp.domain

class GetMushroomUseCase(private val mushroomLocationRepository: MushroomLocationRepository) {

    fun getMushroom(mushroomId: Int): MushroomLocation {
        return mushroomLocationRepository.getMushroom(mushroomId)
    }
}