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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Text
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import com.everytrip.app.feature.region.presentation.detail.RegionDetailScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.everytrip.app.core.designsystem.component.MainTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: RegionSearchViewModel,
    onTitleClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
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

    LaunchedEffect(uiState.selectedRegionId) {
        if (uiState.selectedRegionId != null) {
            bottomSheetState.partialExpand()
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

        if (uiState.selectedRegionId != null) {
            BottomSheetScaffold(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                scaffoldState = bottomSheetScaffoldState,
                sheetPeekHeight = 320.dp,
                sheetContent = {
                    RegionBottomSheet(
                        state = uiState,
                        isExpanded = bottomSheetState.currentValue == SheetValue.Expanded,
                        onPlaceClick = viewModel::selectPlace,
                        onLoadMore = viewModel::loadMorePlaces,
                    )
                },
            ) { innerPadding ->
                RegionKakaoMap(
                    uiState = uiState,
                    onMapError = {},
                    onPlaceClick = viewModel::selectPlace,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        } else {
            RegionKakaoMap(
                uiState = uiState,
                onMapError = {},
                onPlaceClick = viewModel::selectPlace,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
        uiState.regionErrorMessage?.let { Text("지역 정보를 불러오지 못했어요. 지역을 다시 선택해 주세요.") }
    }
    if (uiState.selectedContentId != null) {
        RegionDetailScreen(
            state = uiState,
            onClose = viewModel::closePlaceDetail,
            onRetry = { uiState.selectedContentId?.let(viewModel::selectPlace) },
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
