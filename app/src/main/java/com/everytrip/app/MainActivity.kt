package com.everytrip.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.everytrip.app.feature.region.presentation.detail.RegionDetailScreen
import com.everytrip.app.feature.region.presentation.search.FilteredPlace
import com.everytrip.app.feature.region.presentation.search.RegionSearchScreen
import com.everytrip.app.feature.region.presentation.search.RegionSearchViewModel
import com.everytrip.app.ui.theme.ProjectTheme
import com.kakao.vectormap.KakaoMapSdk

class MainActivity : ComponentActivity() {
    private val regionSearchViewModel: RegionSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            ProjectTheme {
                var selectedPlace by remember { mutableStateOf<FilteredPlace?>(null) }
                val place = selectedPlace

                if (place == null) {
                    RegionSearchScreen(
                        viewModel = regionSearchViewModel,
                        onRegionPlaceClick = { selectedPlace = it },
                    )
                } else {
                    RegionDetailScreen(
                        place = place,
                        onBackClick = { selectedPlace = null },
                    )
                }
            }
        }
    }
}
