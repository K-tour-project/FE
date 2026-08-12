package com.everytrip.app.feature.region.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
fun RegionSearchScreen(
    viewModel: RegionSearchViewModel,
    onRegionPlaceClick: (FilteredPlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mapErrorMessage by remember { mutableStateOf<String?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.onLocationPermissionResult(isGranted = true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RegionKakaoMap(
            uiState = uiState,
            onMapError = { message -> mapErrorMessage = message },
            modifier = Modifier.fillMaxSize(),
        )

        RegionSearchTopBar(
            isLoadingCurrentLocation = uiState.isLoadingCurrentLocation,
            onCurrentLocationClick = {
                if (context.hasLocationPermission()) {
                    viewModel.loadCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp),
        )

        val message = uiState.errorMessage ?: mapErrorMessage
        if (message != null) {
            RegionSearchMessage(
                message = message,
                actionText = if (uiState.errorMessage != null) "위치 권한 허용" else null,
                onRequestPermissionClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 340.dp),
            )
        }

        RegionBottomSheet(
            state = RegionBottomSheetState(),
            onPlaceClick = onRegionPlaceClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun RegionKakaoMap(
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

@Composable
private fun RegionSearchTopBar(
    isLoadingCurrentLocation: Boolean,
    onCurrentLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "지역 검색",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = onCurrentLocationClick,
                enabled = !isLoadingCurrentLocation,
            ) {
                if (isLoadingCurrentLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(10.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "현재 위치",
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionSearchMessage(
    message: String,
    actionText: String?,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (actionText != null) {
                Button(onClick = onRequestPermissionClick) {
                    Text(text = actionText)
                }
            }
        }
    }
}

private fun RegionCoordinate.toLatLng(): LatLng = LatLng.from(latitude, longitude)

private fun Context.hasLocationPermission(): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted || coarseLocationGranted
}

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
