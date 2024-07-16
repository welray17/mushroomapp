package com.example.mushroomapp.presentation

import com.example.mushroomapp.data.MushroomLocationRepositoryImpl
import com.example.mushroomapp.domain.GetMushroomListUseCase
import com.example.mushroomapp.domain.GetMushroomUseCase
import com.example.mushroomapp.domain.MushroomLocation

class MainPresenter(
    private val view: MainContract.View
): MainContract.Presenter {

    private val repository = MushroomLocationRepositoryImpl()
    private val getMushroomListUseCase = GetMushroomListUseCase(repository)
    private val getMushroomUseCase = GetMushroomUseCase(repository)

    override fun getMushroomList(): List<MushroomLocation> {
        val mushroomList = getMushroomListUseCase.getMushroomList()
        return mushroomList
    }

    override fun loadMushroomLocations() {
        view.showMushroomLocations(getMushroomList())
    }

    override fun loadMushroom(mushroomId: Int) {
        val mushroomList = getMushroomUseCase.getMushroom(mushroomId)
    }

}