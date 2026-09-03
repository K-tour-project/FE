package com.everytrip.app.feature.region.presentation.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.everytrip.app.feature.region.data.model.RegionLocation
import com.everytrip.app.feature.region.data.model.RegionPolygon
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.shape.LatLngVertex
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyles
import com.kakao.vectormap.shape.ShapeLayerOptions
import com.kakao.vectormap.shape.ShapeLayerPass

@Composable
fun RegionKakaoMap(
    uiState: RegionSearchUiState,
    onMapError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLocationBitmap = remember { createCurrentLocationBitmap(context) }
    val mapView = remember(context) { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLocationLabel by remember { mutableStateOf<Label?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
    )

    DisposableEffect(mapView) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(exception: Exception) {
                    onMapError(exception.message ?: "지도 화면을 불러오지 못했습니다.")
                }
            },
            object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng {
                    return uiState.currentLocation?.toLatLng() ?: DEFAULT_REGION_POSITION
                }

                override fun getZoomLevel(): Int = DEFAULT_ZOOM_LEVEL

                override fun onMapReady(map: KakaoMap) {
                    kakaoMap = map
                }
            },
        )

        onDispose {
            kakaoMap?.shapeManager?.getLayer(REGION_BOUNDARY_LAYER_ID)?.removeAll()
            mapView.finish()
            kakaoMap = null
            currentLocationLabel = null
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.resume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(kakaoMap, uiState.selectedRegionLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        val selectedRegionLocation = uiState.selectedRegionLocation ?: return@LaunchedEffect

        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(
                selectedRegionLocation.toLatLng(),
                REGION_FILTER_ZOOM_LEVEL,
            ),
        )
    }

    LaunchedEffect(kakaoMap, uiState.selectedRegionId, uiState.selectedRegionPolygons) {
        val map = kakaoMap ?: return@LaunchedEffect
        val shapeManager = map.shapeManager ?: return@LaunchedEffect
        val shapeLayer = shapeManager.getLayer(REGION_BOUNDARY_LAYER_ID)
            ?: shapeManager.addLayer(
                ShapeLayerOptions.from(
                    REGION_BOUNDARY_LAYER_ID,
                    REGION_BOUNDARY_Z_ORDER,
                    ShapeLayerPass.Overlay,
                ),
            )

        shapeLayer.removeAll()
        if (uiState.selectedRegionPolygons.isEmpty()) {
            return@LaunchedEffect
        }

        val polygonStyles = PolygonStyles.from(
            REGION_BOUNDARY_FILL_COLOR,
            REGION_BOUNDARY_STROKE_WIDTH,
            REGION_BOUNDARY_STROKE_COLOR,
        )
        val polygonOptions = PolygonOptions.from(
            "$REGION_BOUNDARY_POLYGON_ID-${uiState.selectedRegionId ?: 0}",
        )
            .setZOrder(REGION_BOUNDARY_Z_ORDER)

        uiState.selectedRegionPolygons.forEach { regionPolygon ->
            polygonOptions.addPolygon(regionPolygon.toMapPoints(), polygonStyles)
        }

        shapeLayer.addPolygon(polygonOptions)
    }

    LaunchedEffect(kakaoMap, uiState.currentLocation) {
        val map = kakaoMap ?: run {
            return@LaunchedEffect
        }
        val currentLocation = uiState.currentLocation ?: run {
            return@LaunchedEffect
        }
        val position = currentLocation.toLatLng()
        val labelLayer = map.labelManager?.layer ?: run {
            return@LaunchedEffect
        }

        currentLocationLabel?.let { labelLayer.remove(it) }
        currentLocationLabel = labelLayer.addLabel(
            LabelOptions.from(CURRENT_LOCATION_LABEL_ID, position)
                .setStyles(
                    LabelStyles.from(
                        LabelStyle.from(currentLocationBitmap)
                            .setAnchorPoint(0.5f, 0.5f),
                    ),
                ),
        )

        map.moveCamera(CameraUpdateFactory.newCenterPosition(position, CURRENT_LOCATION_ZOOM_LEVEL))
    }
}

private fun RegionCoordinate.toLatLng(): LatLng = LatLng.from(latitude, longitude)

private fun RegionLocation.toLatLng(): LatLng = LatLng.from(latitude, longitude)

private fun RegionPolygon.toMapPoints(): MapPoints {
    val mapPoints = MapPoints.fromLatLng(outerBoundary.map { location -> location.toLatLng() })
    if (holes.isNotEmpty()) {
        val holeVertices = holes.map { hole ->
            LatLngVertex.from(hole.map { location -> location.toLatLng() })
        }.toTypedArray()
        mapPoints.setHolePoints(*holeVertices)
    }
    return mapPoints
}

private fun createCurrentLocationBitmap(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (23 * density).toInt()
    val center = size / 2f
    val outerRadius = size * 0.42f
    val innerRadius = size * 0.24f
    val bitmap = createBitmap(width = size, height = size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = Color.argb(58, 30, 136, 229)
    canvas.drawCircle(center, center, outerRadius, paint)
    paint.color = Color.WHITE
    canvas.drawCircle(center, center, innerRadius + 3 * density, paint)
    paint.color = Color.rgb(30, 136, 229)
    canvas.drawCircle(center, center, innerRadius, paint)

    return bitmap
}

private val DEFAULT_REGION_POSITION = LatLng.from(37.5665, 126.9780)

private const val DEFAULT_ZOOM_LEVEL = 15
private const val CURRENT_LOCATION_ZOOM_LEVEL = 16
private const val REGION_FILTER_ZOOM_LEVEL = 11
private const val CURRENT_LOCATION_LABEL_ID = "current-location"
private const val REGION_BOUNDARY_POLYGON_ID = "selected-region-boundary"
private const val REGION_BOUNDARY_LAYER_ID = "selected-region-boundary-layer"
private const val REGION_BOUNDARY_STROKE_WIDTH = 4f
private const val REGION_BOUNDARY_Z_ORDER = 10_000
private val REGION_BOUNDARY_STROKE_COLOR = Color.rgb(29, 114, 248)
private val REGION_BOUNDARY_FILL_COLOR = Color.argb(37, 29, 114, 248)
