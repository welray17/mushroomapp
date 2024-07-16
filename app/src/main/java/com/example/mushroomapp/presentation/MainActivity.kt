package com.example.mushroomapp.presentation

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mushroomapp.R
import com.example.mushroomapp.databinding.ActivityMainBinding
import com.example.mushroomapp.domain.MushroomLocation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Polygon
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.gestures

class MainActivity : AppCompatActivity(), MainContract.View {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var presenter: MainContract.Presenter
    private lateinit var mapView: MapView

    private lateinit var polygonAnnotationManager: PolygonAnnotationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapView = binding.mapView
        presenter = MainPresenter(this)
        setUpMapView()
        presenter.loadMushroomLocations()
        binding.zoomInButton.setOnClickListener {
            zoomIn()
        }
        binding.zoomOutButton.setOnClickListener {
            zoomOut()
        }

        mapView.mapboxMap.subscribeCameraChanged {

        }
    }

    private fun setUpMapView() {
        mapView.mapboxMap.loadStyle(Style.OUTDOORS)
        val locale = resources.configuration.locale
        mapView.mapboxMap.getStyle {
            it.localizeLabels(locale)
            it.addImage("mushroom-icon", BitmapFactory.decodeResource(resources, R.drawable.location))
        }
        mapView.gestures.pinchToZoomEnabled = true
        mapView.mapboxMap.getStyle {
            val geoJsonSource = GeoJsonSource.Builder("mushroom-source")
                .featureCollection(createFeatureCollection())
                .cluster(true)
                .clusterMaxZoom(14) // Максимальный зум для кластеров
                .clusterRadius(50) // Радиус кластеризации в пикселях
                .build()
            it.addSource(geoJsonSource)

            val countLayer = SymbolLayer("SYMBOL_LAYER_COUNT_LAYER_ID", "mushroom-source")
            countLayer
                .iconImage("mushroom-icon") // Имя изображения для значка
                .iconSize(0.2)
                .textField(Expression.toString(get("point_count")))
                .textColor(Color.WHITE)
                .textSize(12.0)
                .textIgnorePlacement(true)
                .textAllowOverlap(true)
            it.addLayer(countLayer)

            val mushroomLayer = SymbolLayer("mushroom-layer", "mushroom-source")
            mushroomLayer
                .iconImage("mushroom-icon")
                .iconSize(0.2)
                .textField(get("mushroomName"))
                .textColor(Color.WHITE)
                .textSize(12.0)
                .filter(Expression.not(Expression.has("point_count")))
//                .textOffset(arrayOf(0, 1.5))
                .textIgnorePlacement(true)
                .textAllowOverlap(true)

            it.addLayer(mushroomLayer)

            val polygonSource = GeoJsonSource.Builder("polygon-source")
                .featureCollection(createPolygonFeatureCollection())
                .build()
            it.addSource(polygonSource)

            val polygonLayer = FillLayer("polygon-layer", "polygon-source")
            polygonLayer
                .fillColor(Color.parseColor("#000000"))
                .fillOpacity(0.3)
                .filter(Expression.lt(Expression.zoom(), Expression.literal(14)))
            it.addLayer(polygonLayer)
            val zoom = mapView.mapboxMap.cameraState.zoom
            val visibility = if (zoom >= 14) Visibility.NONE else Visibility.VISIBLE
            polygonLayer.visibility(visibility)
        }

    }

    private fun createFeatureCollection(): FeatureCollection {
        val features = mutableListOf<Feature>()

        val mushrooms = presenter.getMushroomList()

        mushrooms.forEach {
            val center = getPolygonCentroid(it.polygon)

            val feature = Feature.fromGeometry(center)
            feature.addStringProperty("mushroomName", it.mushroomName)
            features.add(feature)
        }
        return FeatureCollection.fromFeatures(features)
    }

    private fun createPolygonFeatureCollection(): FeatureCollection {
        val features = mutableListOf<Feature>()

        val mushrooms = presenter.getMushroomList()

        mushrooms.forEach {
            val feature = Feature.fromGeometry(it.polygon)
            features.add(feature)
        }

        return FeatureCollection.fromFeatures(features)
    }


    private fun getPolygonCentroid(polygon: Polygon): Point {
        val coordinates = polygon.coordinates()[0]
        var sumLng = 0.0
        var sumLat = 0.0
        coordinates.forEach {
            sumLng += it.longitude()
            sumLat += it.latitude()
        }
        val centroidLng = sumLng / coordinates.size
        val centroidLat = sumLat / coordinates.size
        return Point.fromLngLat(centroidLng, centroidLat)
    }

//    private fun addPolygon(polygon: Polygon) {
//        polygonAnnotationManager =
//            mapView.annotations.createPolygonAnnotationManager(
//                AnnotationConfig()
//            )
//
//        val polygonAnnotationOptions = PolygonAnnotationOptions()
//            .withGeometry(polygon)
//            .withFillColor("#000000")
//            .withFillOpacity(0.3)
//
//        polygonAnnotationManager.create(polygonAnnotationOptions)
//
//    }

    private fun zoomIn() {
        val currentZoom = mapView.mapboxMap.cameraState.zoom
        mapView.mapboxMap.easeTo(
            CameraOptions.Builder()
                .zoom(currentZoom + 0.7)
                .build(),
            MapAnimationOptions.Builder()
                .duration(700)
                .build()
        )
    }

    private fun zoomOut() {
        val currentZoom = mapView.mapboxMap.cameraState.zoom
        mapView.mapboxMap.easeTo(
            CameraOptions.Builder()
                .zoom(currentZoom - 0.7)
                .build(),
            MapAnimationOptions.Builder()
                .duration(700)
                .build()
        )
    }

    override fun showMushroomLocations(mushroomLocations: List<MushroomLocation>) {
        for (mushroom in mushroomLocations) {
//            addPolygon(mushroom.polygon)
        }
    }
}