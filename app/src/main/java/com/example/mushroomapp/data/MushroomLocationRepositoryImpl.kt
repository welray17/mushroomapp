package com.example.mushroomapp.data

import com.example.mushroomapp.domain.MushroomLocation
import com.example.mushroomapp.domain.MushroomLocationRepository
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon

class MushroomLocationRepositoryImpl: MushroomLocationRepository {

    private val mushroomList = listOf(
        MushroomLocation(
            1,
            "Место сбора1",
            123,
            Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(76.92748, 43.25654),
                        Point.fromLngLat(76.92748, 42.25674),
                        Point.fromLngLat(77.92768, 44.25674),
                    )
                )
            ),
            "Место для сбора №1. Прездазначен для сбора грибов",
            "Алматинская область",
            "Белые грибы, шампионы, лисички",
            "8 (777) 777 77 77",
            "qwerty@email.com"
        ),
        MushroomLocation(
            1,
            "Место сбора2",
            456,
            Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(76.92748, 43.24654),
                        Point.fromLngLat(75.92748, 42.32674),
                        Point.fromLngLat(77.87768, 44.23674),
                    )
                )
            ),
            "Место для сбора №2. Прездазначен для сбора грибов",
            "Алматинская область",
            "Белые грибы, шампионы",
            "8 (777) 777 77 77",
            "qwerty@email.com"
        ),
        MushroomLocation(
            1,
            "Место сбора3",
            456,
            Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(74.92748, 45.24654),
                        Point.fromLngLat(73.92748, 43.34674),
                        Point.fromLngLat(73.87768, 44.23674),
                        Point.fromLngLat(72.87768, 45.23674),
                    )
                )
            ),
            "Место для сбора №2. Прездазначен для сбора грибов",
            "Алматинская область",
            "Белые грибы, шампионы",
            "8 (777) 777 77 77",
            "qwerty@email.com"
        )
    )

    override fun getMushroom(id: Int): MushroomLocation {
        return mushroomList.get(id)
    }

    override fun getMushroomList(): List<MushroomLocation> {
        return mushroomList
    }
}