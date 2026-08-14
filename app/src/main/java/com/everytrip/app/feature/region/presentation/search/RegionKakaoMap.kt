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

@Composable
fun RegionKakaoMap(
    uiState: RegionSearchUiState,
    onMapError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLocationBitmap = remember { createCurrentLocationBitmap(context) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLocationLabel by remember { mutableStateOf<Label?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            MapView(viewContext).also { view ->
                mapView = view
                view.start(
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
            }
        },
    )

    DisposableEffect(mapView, lifecycleOwner) {
        val view = mapView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> view?.resume()
                Lifecycle.Event.ON_PAUSE -> view?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            view?.finish()
            mapView = null
            kakaoMap = null
            currentLocationLabel = null
        }
    }

    LaunchedEffect(kakaoMap, uiState.currentLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        val currentLocation = uiState.currentLocation ?: return@LaunchedEffect
        val position = currentLocation.toLatLng()
        val labelLayer = map.labelManager?.layer ?: return@LaunchedEffect

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

private fun createCurrentLocationBitmap(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (28 * density).toInt()
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
private const val CURRENT_LOCATION_LABEL_ID = "current-location"
