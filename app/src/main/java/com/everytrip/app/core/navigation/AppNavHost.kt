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
import com.everytrip.app.core.designsystem.component.AppBottomNavigationBar
import com.everytrip.app.core.designsystem.component.NetworkErrorDialog
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchScreen
import com.everytrip.app.feature.auth.presentation.login.LoginScreen
import com.everytrip.app.feature.auth.presentation.login.LoginViewModel
import com.everytrip.app.feature.auth.presentation.login.SessionCheckState
import com.everytrip.app.feature.auth.presentation.signup.SignUpScreen
import com.everytrip.app.feature.auth.presentation.signup.SignUpViewModel
import com.everytrip.app.feature.chatbot.presentation.AiChatbotScreen
import com.everytrip.app.feature.chatbot.presentation.ChatViewModel
import com.everytrip.app.feature.home.presentation.HomeScreen
import com.everytrip.app.feature.mypage.presentation.FavoriteViewModel
import com.everytrip.app.feature.mypage.presentation.MyPageRoute
import com.everytrip.app.feature.region.presentation.search.RegionSearchScreen
import com.everytrip.app.feature.region.presentation.search.RegionSearchViewModel
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
    var isGuestMode by remember { mutableStateOf(false) }

    LaunchedEffect(signUpUiState.signupCompleted) {
        if (signUpUiState.signupCompleted) {
            navController.navigate(AppRoute.Login.route) {
                popUpTo(AppRoute.SignUp.route) { inclusive = true }
                launchSingleTop = true
            }
            signUpViewModel.consumeSignupCompleted()
        }
    }

    LaunchedEffect(loginUiState.sessionCheckState, isGuestMode) {
        when {
            loginUiState.sessionCheckState == SessionCheckState.Authenticated || isGuestMode -> {
                if (loginUiState.sessionCheckState == SessionCheckState.Authenticated) {
                    favoriteViewModel.refresh()
                }
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
        SessionCheckState.Checking -> LoadingScreen()
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

            Scaffold(
                bottomBar = {
                    if (selectedBottomIndex >= 0 && !isImeVisible) {
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
                            onGuestLoginClick = { isGuestMode = true },
                            onKakaoLoginClick = onKakaoLoginClick,
                            onGoogleLoginClick = onGoogleLoginClick,
                            onMessageShown = loginViewModel::clearMessage,
                        )
                    }
                    composable(AppRoute.SignUp.route) {
                        SignUpScreen(
                            uiState = signUpUiState,
                            onBackClick = { navController.popBackStack() },
                            onSendCodeClick = signUpViewModel::sendCode,
                            onVerifyCodeClick = signUpViewModel::verifyCode,
                            onSignUpClick = signUpViewModel::signUp,
                            onMessageShown = signUpViewModel::clearMessage,
                        )
                    }
                    composable(AppRoute.Home.route) {
                        HomeScreen(
                            onNavigateToSearch = { navController.navigateBottom(AppRoute.ArtworkSearch) },
                            onNavigateToRegionSearch = { navController.navigateBottom(AppRoute.RegionSearch) },
                        )
                    }
                    composable(AppRoute.Chatbot.route) { AiChatbotScreen(chatViewModel) }
                    composable(AppRoute.ArtworkSearch.route) { ArtworkSearchScreen() }
                    composable(AppRoute.RegionSearch.route) {
                        RegionSearchScreen(
                            viewModel = regionSearchViewModel,
                            onTitleClick = { navController.navigateBottom(AppRoute.Home) },
                            favoriteState = favoriteUiState,
                            onTogglePlace = favoriteViewModel::togglePlace,
                            onToggleTourism = favoriteViewModel::toggleTourism,
                            onToggleProduct = favoriteViewModel::toggleProduct,
                        )
                    }
                    composable(AppRoute.MyPage.route) { MyPageRoute(favoriteViewModel) }
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

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryBlue)
    }
}
