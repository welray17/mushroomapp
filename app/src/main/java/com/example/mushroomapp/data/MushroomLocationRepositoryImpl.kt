package com.example.mushroomapp.data

import com.example.mushroomapp.domain.MushroomLocation
import com.example.mushroomapp.domain.MushroomLocationRepository
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon

class MushroomLocationRepositoryImpl: MushroomLocationRepository {

    private val mushroomList = listOf(
        MushroomLocation(
            1,
            "Белый гриб",
            123,
            Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(76.92748, 43.25654),
                        Point.fromLngLat(76.92748, 42.25674),
                        Point.fromLngLat(77.92768, 44.25674),
                    )
                )
            )
        ),
        MushroomLocation(
            1,
            "Шампиньоны",
            123,
            Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(76.92748, 43.24654),
                        Point.fromLngLat(75.92748, 42.32674),
                        Point.fromLngLat(77.87768, 44.23674),
                    )
                )
            )
        )
    )

    override fun getMushroom(id: Int): MushroomLocation {
        return mushroomList.get(id)
    }

    override fun getMushroomList(): List<MushroomLocation> {
        return mushroomList
    }
}