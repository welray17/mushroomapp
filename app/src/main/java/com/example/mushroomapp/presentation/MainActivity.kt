package com.example.mushroomapp.presentation

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mushroomapp.R
import com.example.mushroomapp.databinding.ActivityMainBinding
import com.example.mushroomapp.domain.MushroomLocation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Polygon
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconTextFit
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.animation.*
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.gestures

class MainActivity : AppCompatActivity(), MainContract.View {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var presenter: MainContract.Presenter
    val gson = Gson()
    private lateinit var mapView: MapView
    private val mapboxMap by lazy {
        mapView.mapboxMap
    }

    private lateinit var pointAnnotationManager: PointAnnotationManager
    private  var annotationList = ArrayList<PointAnnotationOptions>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapView = binding.mapView
        presenter = MainPresenter(this)
        presenter.loadMushroomLocations()
        binding.zoomInButton.setOnClickListener {
            zoomIn()
        }
        binding.zoomOutButton.setOnClickListener {
            zoomOut()
        }

        mapboxMap.subscribeCameraChanged {
            Log.d("zoom", "${mapView.mapboxMap.cameraState.zoom}")
            hidePolygons()
            hideIcons()
            changeIcons()
        }
    }

    private fun changeIcons() {
        mapboxMap.getStyle {
            val zoom = mapboxMap.cameraState.zoom
            if (zoom > 8.5){

                pointAnnotationManager.annotations.forEach { annotation ->
                    annotation.iconImage = "location-icon"
                    annotation.textColorInt = Color.WHITE
                    annotation.iconSize = null
                    annotation.iconTextFit = IconTextFit.BOTH
                    pointAnnotationManager.update(annotation)
                }
            }

            else if (zoom in 7.0..8.5) {
                if (pointAnnotationManager.annotations.isEmpty())
                    pointAnnotationManager.create(annotationList)

                pointAnnotationManager.annotations.forEach { annotation ->
                    annotation.iconImage = "point-icon"
                    annotation.textColorInt = Color.TRANSPARENT
                    annotation.iconSize = 0.1
                    annotation.iconTextFit = IconTextFit.NONE
                    pointAnnotationManager.update(annotation)
                }
            }
        }
    }

    private fun hideIcons() {
        mapboxMap.getStyle {
            val zoom = mapboxMap.cameraState.zoom
            if (pointAnnotationManager.annotations.isNotEmpty() && zoom<=7.0) {
                pointAnnotationManager.deleteAll()
            }
            else if (zoom in 7.0..8.5 && pointAnnotationManager.annotations.isEmpty()) {
                pointAnnotationManager.create(annotationList)
            }
        }
    }

    private fun hidePolygons() {
        mapboxMap.getStyle {
            val zoom = mapboxMap.cameraState.zoom
            val polygonLayer = mapView.mapboxMap.getLayer("polygon-layer") as FillLayer
            if (zoom < 6)
                polygonLayer.visibility(Visibility.NONE)
            else
                polygonLayer.visibility(Visibility.VISIBLE)
        }
    }

    private fun setUpMapView() {
        mapboxMap.loadStyle(Style.OUTDOORS)
        val locale = resources.configuration.locale
        mapboxMap.getStyle {
            it.localizeLabels(locale)
            mapView.gestures.pinchToZoomEnabled = true
            it.addImage(
                "location-icon",
                BitmapFactory.decodeResource(resources, R.drawable.final_pin)
            )
            it.addImage(
                "point-icon",
                BitmapFactory.decodeResource(resources, R.drawable.img_1)
            )
        }
        addPolygonLayer()
        addClusterLayer()
        createPoints()
    }

    private fun addClusterLayer() {
        mapboxMap.getStyle {
            val geoJsonSource = GeoJsonSource.Builder("mushroom-source")
                .featureCollection(createFeatureCollection(false))
                .cluster(true)
                .clusterMaxZoom(14)
                .clusterRadius(50)
                .build()
            it.addSource(geoJsonSource)

            val countLayer = SymbolLayer("count-layer", "mushroom-source")
            countLayer
                .iconImage("location-icon")
                .textField(Expression.toString(get("point_count")))
                .textFont(get("Arial Unicode MS Bold"))
                .textColor(Color.WHITE)
                .textSize(12.0)
                .textJustify(TextJustify.CENTER)
                .textIgnorePlacement(true)
                .iconTextFit(IconTextFit.BOTH)
                .iconTextFitPadding(listOf(20.0,40.0,20.0,40.0))
                .textAllowOverlap(true)
            it.addLayer(countLayer)

            val mushroomLayer = SymbolLayer("mushroom-layer", "mushroom-source")
            mushroomLayer
                .iconImage("location-icon")
                .iconSize(0.0)
                .textColor(Color.WHITE)
                .textSize(12.0)
                .filter(Expression.not(Expression.has("point_count")))
                .textIgnorePlacement(true)
                .textJustify(TextJustify.CENTER)
                .textAllowOverlap(true)
            it.addLayer(mushroomLayer)
        }
    }

    private fun addPolygonLayer() {
        mapboxMap.getStyle {
            val polygonSource = GeoJsonSource.Builder("polygon-source")
                .featureCollection(createFeatureCollection(true))
                .build()
            it.addSource(polygonSource)

            val polygonLayer = FillLayer("polygon-layer", "polygon-source")
            polygonLayer
                .fillColor(Color.BLACK)
                .fillOpacity(0.3)
                .slot("bottom")
            it.addLayerBelow(polygonLayer, "points-layer")
        }
    }

    private fun createPoints() {
        val annotationConfig = AnnotationConfig(
            layerId = "points-layer"
        )
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager(annotationConfig)
        val mushrooms = presenter.getMushroomList()
        mushrooms.forEach {
            val jsonData = gson.toJsonTree(it)
            val iconAnnotationOptions = PointAnnotationOptions()
                .withPoint(getPolygonCentroid(it.polygon))
                .withIconImage("location-icon")
                .withTextField(it.areaName)
                .withTextColor(Color.WHITE)
                .withTextSize(12.0)
                .withTextJustify(TextJustify.CENTER)
                .withIconTextFit(IconTextFit.BOTH)
                .withIconTextFitPadding(listOf(20.0,90.0,20.0,90.0))
                .withData(jsonData)
            annotationList.add(iconAnnotationOptions)
        }
        addClickListenersToPoints()
    }

    private fun addClickListenersToPoints() {
        pointAnnotationManager.addClickListener(OnPointAnnotationClickListener {
            val jsonData = it.getData()!!
            val mushroom = gson.fromJson(jsonData, MushroomLocation::class.java)
            Log.d("ClickTest", mushroom.areaName)
            mapboxMap.easeTo(
                CameraOptions.Builder()
                    .zoom(10.0)
                    .center(it.point)
                    .build(),
                MapAnimationOptions.Builder()
                    .duration(700)
                    .build()
            )
            showBottomSheet(mushroom)
            true
        })
    }

    private fun showBottomSheet(mushroom: MushroomLocation) {
        val dialog = BottomSheetDialog(this)
//        dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        dialog.behavior.peekHeight = 500
        dialog.behavior.isDraggable = true
        dialog.setCanceledOnTouchOutside(true)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val tvName = bottomSheetView.findViewById<TextView>(R.id.name)
        tvName.text = mushroom.areaName
        val tvAddress = bottomSheetView.findViewById<TextView>(R.id.address)
        tvAddress.text = mushroom.address
        val dragView = bottomSheetView
            .findViewById<BottomSheetDragHandleView>(R.id.dragView)

        dialog.setContentView(bottomSheetView)
        dialog.show()
        dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val expandedContent = bottomSheetView
                    .findViewById<ConstraintLayout>(R.id.expandedContent)
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    expandedContent.visibility = View.VISIBLE
                    setTextsToTextFieldsInBottomSheet(mushroom, expandedContent)

                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    expandedContent.visibility = View.GONE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun setTextsToTextFieldsInBottomSheet(mushroom: MushroomLocation, expandedContent: ConstraintLayout) {
        val area = expandedContent.findViewById<TextView>(R.id.area)
        area.text = String.format(
            getString(R.string.area), mushroom.area
        )
        val description = expandedContent.findViewById<TextView>(R.id.descriptiontv)
        description.text = mushroom.description
        val phone = expandedContent.findViewById<TextView>(R.id.phoneNumber)
        phone.text = mushroom.phoneNumber
        val email = expandedContent.findViewById<TextView>(R.id.email)
        email.text = mushroom.email
        val address = expandedContent.findViewById<TextView>(R.id.location)
        address.text = mushroom.address
        val mushroomsList = expandedContent.findViewById<TextView>(R.id.mushroomsList)
        mushroomsList.text = mushroom.mushrooms
    }

    private fun createFeatureCollection(isItPolygon: Boolean): FeatureCollection {
        val features = mutableListOf<Feature>()
        val mushrooms = presenter.getMushroomList()
        var feature: Feature
        mushrooms.forEach {
            if (isItPolygon) {
                feature = Feature.fromGeometry(it.polygon)
            } else {
                val center = getPolygonCentroid(it.polygon)
                feature = Feature.fromGeometry(center)
            }


            feature.addStringProperty("mushroomName", it.areaName)
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

    private fun zoomIn() {
        val currentZoom = mapboxMap.cameraState.zoom
        mapboxMap.easeTo(
            CameraOptions.Builder()
                .zoom(currentZoom + 0.7)
                .build(),
            MapAnimationOptions.Builder()
                .duration(700)
                .build()
        )
    }

    private fun zoomOut() {
        val currentZoom = mapboxMap.cameraState.zoom
        mapboxMap.easeTo(
            CameraOptions.Builder()
                .zoom(currentZoom - 0.7)
                .build(),
            MapAnimationOptions.Builder()
                .duration(700)
                .build()
        )
    }

    override fun showMushroomLocations(mushroomLocations: List<MushroomLocation>) {
        setUpMapView()
    }
}