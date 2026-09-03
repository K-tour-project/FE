package com.everytrip.app.feature.region.presentation.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.everytrip.app.core.designsystem.component.MainTopBar

@Composable
fun RegionSearchScreen(
    viewModel: RegionSearchViewModel,
    onRegionPlaceClick: (FilteredPlace) -> Unit,
    onTitleClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        MainTopBar(
            title = "Every Trip",
            icon = Icons.Default.NotificationsNone,
            iconContentDescription = "알림",
            onIconClick = { /* 알림 클릭 */ },
            onTitleClick = onTitleClick,
            showBadge = true
        )

        RegionFilterBar(
            state = RegionFilterState(
                province = uiState.selectedSido?.name.orEmpty(),
                city = uiState.selectedSigungu?.name.orEmpty(),
            ),
            provinceOptions = uiState.sidos.map { it.name },
            cityOptions = uiState.sigungus.map { it.name },
            isCityEnabled = uiState.selectedSido?.hasChildren == true,
            isProvinceLoading = uiState.isLoadingSidos,
            isCityLoading = uiState.isLoadingSigungus,
            onProvinceDropdownClick = viewModel::loadSidos,
            onProvinceSelected = { province ->
                viewModel.onSidoSelected(province)
            },
            onCitySelected = { city ->
                viewModel.onSigunguSelected(city)
            },
            modifier = Modifier.fillMaxWidth(),
        )

        RegionKakaoMap(
            uiState = uiState,
            onMapError = {},
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

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
