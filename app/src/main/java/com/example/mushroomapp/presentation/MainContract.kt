package com.example.mushroomapp.presentation

import com.example.mushroomapp.domain.MushroomLocation

class MainContract {

    interface View {
        fun showMushroomLocations(mushroomLocations: List<MushroomLocation>)
    }

    interface Presenter {
        fun loadMushroomLocations()
        fun getMushroomList(): List<MushroomLocation>
        fun loadMushroom(mushroomId: Int)
    }
}