package com.everytrip.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everytrip.app.core.designsystem.component.AppBottomNavigationBar
import com.everytrip.app.core.designsystem.component.NetworkErrorDialog
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchScreen
import com.everytrip.app.feature.artwork.presentation.detail.ArtworkDetailRoute
import com.everytrip.app.feature.auth.presentation.login.LoginScreen
import com.everytrip.app.feature.auth.presentation.login.LoginViewModel
import com.everytrip.app.feature.auth.presentation.login.SessionCheckState
import com.everytrip.app.feature.auth.presentation.signup.SignUpScreen
import com.everytrip.app.feature.auth.presentation.signup.SignUpViewModel
import com.everytrip.app.feature.auth.presentation.legal.PrivacyPolicyScreen
import com.everytrip.app.feature.auth.presentation.legal.TermsOfServiceScreen
import com.everytrip.app.feature.chatbot.presentation.AiChatbotScreen
import com.everytrip.app.feature.chatbot.presentation.ChatViewModel
import com.everytrip.app.feature.home.presentation.HomeScreen
import com.everytrip.app.feature.home.presentation.HomeViewModel
import com.everytrip.app.feature.mypage.presentation.FavoriteViewModel
import com.everytrip.app.feature.mypage.data.FavoritePlaceData
import com.everytrip.app.feature.mypage.presentation.MyPageRoute
import com.everytrip.app.feature.mypage.presentation.SettingsRoute
import com.everytrip.app.feature.region.presentation.search.RegionSearchScreen
import com.everytrip.app.feature.region.presentation.search.RegionSearchViewModel
import com.everytrip.app.feature.region.presentation.detail.RegionDetailScreen
import com.everytrip.app.feature.splash.presentation.SplashScreen
import com.everytrip.app.ui.theme.PrimaryBlue

@Composable
fun AppNavHost(
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    regionSearchViewModel: RegionSearchViewModel,
    chatViewModel: ChatViewModel,
    favoriteViewModel: FavoriteViewModel,
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onExitClick: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val signUpUiState by signUpViewModel.uiState.collectAsState()
    val favoriteUiState by favoriteViewModel.uiState.collectAsState()
    val regionUiState by regionSearchViewModel.uiState.collectAsState()

    LaunchedEffect(signUpUiState.signupCompleted) {
        if (signUpUiState.signupCompleted) {
            navController.navigate(AppRoute.Login.route) {
                popUpTo(AppRoute.SignUp.route) { inclusive = true }
                launchSingleTop = true
            }
            signUpViewModel.consumeSignupCompleted()
        }
    }

    LaunchedEffect(loginUiState.sessionCheckState) {
        when {
            loginUiState.sessionCheckState == SessionCheckState.Authenticated -> {
                favoriteViewModel.refresh()
                navController.navigate(AppRoute.Home.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            loginUiState.sessionCheckState == SessionCheckState.Unauthenticated -> {
                navController.navigate(AppRoute.Login.route) { launchSingleTop = true }
            }
        }
    }

    when (loginUiState.sessionCheckState) {
        SessionCheckState.Checking -> SplashScreen()
        SessionCheckState.RetryableError -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
                NetworkErrorDialog(
                    onExitClick = onExitClick,
                    onRetryClick = loginViewModel::checkExistingSession,
                )
            }
        }
        else -> {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val selectedBottomIndex = AppRoute.bottomRoutes.indexOfFirst { it.route == currentRoute }
            val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
            val isInitialPlacesError = currentRoute == AppRoute.RegionSearch.route &&
                regionUiState.places.isEmpty() && regionUiState.placesErrorMessage != null

            Scaffold(
                bottomBar = {
                    if (selectedBottomIndex >= 0 && !isImeVisible && !isInitialPlacesError) {
                        AppBottomNavigationBar(
                            selectedIndex = selectedBottomIndex,
                            onItemSelected = { index ->
                                navController.navigate(AppRoute.bottomRoutes[index].route) {
                                    popUpTo(AppRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Login.route,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    composable(AppRoute.Login.route) {
                        LoginScreen(
                            uiState = loginUiState,
                            onBackClick = {},
                            onLoginClick = loginViewModel::login,
                            onNavigateToSignUp = { navController.navigate(AppRoute.SignUp.route) },
                            onKakaoLoginClick = onKakaoLoginClick,
                            onGoogleLoginClick = onGoogleLoginClick,
                            onMessageShown = loginViewModel::clearMessage,
                            resetState = loginViewModel.resetState.collectAsState().value,
                            onResetEmailChanged = loginViewModel::resetEmailChanged,
                            onSendResetCode = loginViewModel::sendPasswordResetCode,
                            onVerifyResetCode = loginViewModel::verifyPasswordResetCode,
                            onConfirmReset = loginViewModel::confirmPasswordReset,
                            onDismissReset = loginViewModel::dismissPasswordReset,
                        )
                    }
                    composable(AppRoute.SignUp.route) {
                        SignUpScreen(
                            uiState = signUpUiState,
                            onBackClick = { navController.popBackStack() },
                            onSendCodeClick = signUpViewModel::sendCode,
                            onVerifyCodeClick = signUpViewModel::verifyCode,
                            onSignUpClick = signUpViewModel::signUp,
                            onPrivacyClick = { navController.navigate(AppRoute.PrivacyPolicy.route) },
                            onTermsClick = { navController.navigate(AppRoute.TermsOfService.route) },
                            onMessageShown = signUpViewModel::clearMessage,
                        )
                    }
                    composable(AppRoute.PrivacyPolicy.route) {
                        PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable(AppRoute.TermsOfService.route) {
                        TermsOfServiceScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable(AppRoute.Home.route) {
                        val homeViewModel: HomeViewModel = viewModel()
                        val homeUiState by homeViewModel.uiState.collectAsState()
                        var showPopularPlaceDetail by remember { mutableStateOf(false) }
                        HomeScreen(
                            uiState = homeUiState,
                            onNavigateToSearch = { navController.navigateBottom(AppRoute.ArtworkSearch) },
                            onNavigateToChatbot = { navController.navigateBottom(AppRoute.Chatbot) },
                            onNavigateToRegionSearch = { navController.navigateBottom(AppRoute.RegionSearch) },
                            onNavigateToDestination = { detailPath ->
                                val contentId = detailPath.substringAfterLast('/').takeIf(String::isNotBlank)
                                if (detailPath.startsWith("/tourism-places/") && contentId != null) {
                                    regionSearchViewModel.selectRelatedPlace(contentId)
                                    showPopularPlaceDetail = true
                                }
                            },
                            onNavigateToWork = { detailPath ->
                                detailPath.substringAfterLast('/').toIntOrNull()?.let { productId ->
                                    navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                                }
                            },
                            onRetry = homeViewModel::loadHome,
                        )
                        if (showPopularPlaceDetail) {
                            RegionDetailScreen(
                                state = regionUiState,
                                onClose = {
                                    showPopularPlaceDetail = false
                                    regionSearchViewModel.closePlaceDetail()
                                },
                                onRetry = {
                                    regionUiState.selectedContentId?.let(regionSearchViewModel::selectRelatedPlace)
                                },
                                onContentClick = { productId ->
                                    showPopularPlaceDetail = false
                                    regionSearchViewModel.closePlaceDetail()
                                    navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                                },
                                onFilmingPlaceClick = regionSearchViewModel::selectFilmingPlace,
                                onRelatedPlaceClick = regionSearchViewModel::selectRelatedPlace,
                                favoritePlaceIds = favoriteUiState.favoritePlaceIds,
                                favoriteTourismIds = favoriteUiState.favoriteTourismIds,
                                savedProductIds = favoriteUiState.savedProductIds,
                                onTogglePlace = favoriteViewModel::togglePlace,
                                onToggleTourism = favoriteViewModel::toggleTourism,
                                onToggleProduct = favoriteViewModel::toggleProduct,
                            )
                        }
                    }
                    composable(AppRoute.Chatbot.route) { AiChatbotScreen(chatViewModel) }
                    composable(AppRoute.ArtworkSearch.route) {
                        ArtworkSearchScreen(
                            onCancelClick = { navController.popBackStack() },
                            onArtworkClick = { productId ->
                                navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                            },
                        )
                    }
                    composable(AppRoute.RegionSearch.route) {
                        RegionSearchScreen(
                            viewModel = regionSearchViewModel,
                            onTitleClick = { navController.navigateBottom(AppRoute.Home) },
                            onBackClick = {
                                if (!navController.popBackStack()) {
                                    navController.navigateBottom(AppRoute.Home)
                                }
                            },
                            favoriteState = favoriteUiState,
                            onTogglePlace = favoriteViewModel::togglePlace,
                            onToggleTourism = favoriteViewModel::toggleTourism,
                            onToggleProduct = favoriteViewModel::toggleProduct,
                            onArtworkClick = { productId ->
                                regionSearchViewModel.closePlaceDetail()
                                navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                            },
                        )
                    }
                    composable(AppRoute.MyPage.route) {
                        var selectedMyPageFavorite by remember { mutableStateOf<FavoritePlaceData?>(null) }
                        var isSelectedMyPageFavorite by remember { mutableStateOf(true) }
                        var favoriteUpdatePending by remember { mutableStateOf(false) }
                        MyPageRoute(
                            viewModel = favoriteViewModel,
                            onSettingsClick = { navController.navigate(AppRoute.Settings.route) },
                            onPlaceClick = { favorite ->
                                val detailPath = favorite.detailPath
                                val id = detailPath.substringAfterLast('/').takeIf(String::isNotBlank)
                                when {
                                    detailPath.startsWith("/places/") && id?.toIntOrNull() != null -> {
                                        regionSearchViewModel.selectFilmingPlace(id.toInt())
                                        selectedMyPageFavorite = favorite
                                        isSelectedMyPageFavorite = true
                                    }
                                    detailPath.startsWith("/tourism-places/") && id != null -> {
                                        regionSearchViewModel.selectRelatedPlace(id)
                                        selectedMyPageFavorite = favorite
                                        isSelectedMyPageFavorite = true
                                    }
                                }
                            },
                            onWorkClick = { detailPath ->
                                detailPath.substringAfterLast('/').toIntOrNull()?.let { productId ->
                                    navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                                }
                            },
                        )
                        selectedMyPageFavorite?.let { selected ->
                            val selectedPlaceId = selected.placeId
                                ?: selected.detailPath.takeIf { it.startsWith("/places/") }
                                    ?.substringAfterLast('/')?.toIntOrNull()
                            val selectedTourismId = selected.contentId
                                ?: selected.detailPath.takeIf { it.startsWith("/tourism-places/") }
                                    ?.substringAfterLast('/')
                            RegionDetailScreen(
                                state = regionUiState,
                                onClose = {
                                    selectedMyPageFavorite = null
                                    regionSearchViewModel.closePlaceDetail()
                                },
                                onRetry = {
                                    regionUiState.selectedPlaceId?.let(regionSearchViewModel::selectFilmingPlace)
                                        ?: regionUiState.selectedContentId?.let(regionSearchViewModel::selectRelatedPlace)
                                },
                                onContentClick = { productId ->
                                    selectedMyPageFavorite = null
                                    regionSearchViewModel.closePlaceDetail()
                                    navController.navigate(AppRoute.ArtworkDetail.createRoute(productId))
                                },
                                onFilmingPlaceClick = regionSearchViewModel::selectFilmingPlace,
                                onRelatedPlaceClick = regionSearchViewModel::selectRelatedPlace,
                                favoritePlaceIds = selectedPlaceId?.let { placeId ->
                                    if (isSelectedMyPageFavorite) favoriteUiState.favoritePlaceIds + placeId
                                    else favoriteUiState.favoritePlaceIds - placeId
                                } ?: favoriteUiState.favoritePlaceIds,
                                favoriteTourismIds = selectedTourismId?.let { contentId ->
                                    if (isSelectedMyPageFavorite) favoriteUiState.favoriteTourismIds + contentId
                                    else favoriteUiState.favoriteTourismIds - contentId
                                } ?: favoriteUiState.favoriteTourismIds,
                                savedProductIds = favoriteUiState.savedProductIds,
                                onTogglePlace = { placeId ->
                                    if (placeId == selectedPlaceId) {
                                        if (!favoriteUpdatePending) {
                                            val previous = isSelectedMyPageFavorite
                                            isSelectedMyPageFavorite = !previous
                                            favoriteUpdatePending = true
                                            favoriteViewModel.setPlaceFavorite(placeId, !previous) { success ->
                                                if (!success) isSelectedMyPageFavorite = previous
                                                favoriteUpdatePending = false
                                            }
                                        }
                                    } else favoriteViewModel.togglePlace(placeId)
                                },
                                onToggleTourism = { contentId ->
                                    if (contentId == selectedTourismId) {
                                        if (!favoriteUpdatePending) {
                                            val previous = isSelectedMyPageFavorite
                                            isSelectedMyPageFavorite = !previous
                                            favoriteUpdatePending = true
                                            favoriteViewModel.setTourismFavorite(contentId, !previous) { success ->
                                                if (!success) isSelectedMyPageFavorite = previous
                                                favoriteUpdatePending = false
                                            }
                                        }
                                    } else favoriteViewModel.toggleTourism(contentId)
                                },
                                onToggleProduct = favoriteViewModel::toggleProduct,
                            )
                        }
                    }
                    composable(AppRoute.Settings.route) {
                        SettingsRoute(
                            viewModel = favoriteViewModel,
                            authProvider = loginUiState.user?.authProvider,
                            onBackClick = { navController.popBackStack() },
                            onPrivacyClick = { navController.navigate(AppRoute.PrivacyPolicy.route) },
                            onTermsClick = { navController.navigate(AppRoute.TermsOfService.route) },
                            onLogoutClick = {
                                loginViewModel.logout()
                            },
                        )
                    }
                    composable(
                        route = AppRoute.ArtworkDetail.route,
                        arguments = listOf(navArgument("productId") { type = NavType.IntType }),
                    ) { entry ->
                        val productId = entry.arguments?.getInt("productId") ?: return@composable
                        var showFilmingPlaceDetail by remember(productId) { mutableStateOf(false) }
                        ArtworkDetailRoute(
                            productId = productId,
                            onBackClick = { navController.popBackStack() },
                            onFilmingPlaceClick = { placeId ->
                                regionSearchViewModel.selectFilmingPlace(placeId)
                                showFilmingPlaceDetail = true
                            },
                            onRelatedArtworkClick = { relatedId ->
                                navController.navigate(AppRoute.ArtworkDetail.createRoute(relatedId))
                            },
                            isSaved = { it in favoriteUiState.savedProductIds },
                            onSaveClick = favoriteViewModel::toggleProduct,
                        )
                        if (showFilmingPlaceDetail) {
                            RegionDetailScreen(
                                state = regionUiState,
                                onClose = {
                                    showFilmingPlaceDetail = false
                                    regionSearchViewModel.closePlaceDetail()
                                },
                                onRetry = {
                                    regionUiState.selectedPlaceId?.let(regionSearchViewModel::selectFilmingPlace)
                                        ?: regionUiState.selectedContentId?.let(regionSearchViewModel::selectRelatedPlace)
                                },
                                onContentClick = { relatedId ->
                                    showFilmingPlaceDetail = false
                                    regionSearchViewModel.closePlaceDetail()
                                    navController.navigate(AppRoute.ArtworkDetail.createRoute(relatedId))
                                },
                                onFilmingPlaceClick = regionSearchViewModel::selectFilmingPlace,
                                onRelatedPlaceClick = regionSearchViewModel::selectRelatedPlace,
                                favoritePlaceIds = favoriteUiState.favoritePlaceIds,
                                favoriteTourismIds = favoriteUiState.favoriteTourismIds,
                                savedProductIds = favoriteUiState.savedProductIds,
                                onTogglePlace = favoriteViewModel::togglePlace,
                                onToggleTourism = favoriteViewModel::toggleTourism,
                                onToggleProduct = favoriteViewModel::toggleProduct,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateBottom(route: AppRoute) {
    navigate(route.route) {
        popUpTo(AppRoute.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
