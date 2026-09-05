package com.everytrip.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.everytrip.app.feature.auth.presentation.login.LoginScreen
import com.everytrip.app.feature.auth.presentation.login.LoginViewModel
import com.everytrip.app.feature.auth.presentation.signup.SignUpScreen
import com.everytrip.app.feature.auth.presentation.signup.SignUpViewModel
import com.everytrip.app.feature.home.presentation.HomeScreen
import com.everytrip.app.feature.region.presentation.detail.RegionDetailScreen
import com.everytrip.app.feature.region.presentation.search.FilteredPlace
import com.everytrip.app.feature.region.presentation.search.RegionSearchScreen
import com.everytrip.app.feature.region.presentation.search.RegionSearchViewModel
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()
    private val regionSearchViewModel: RegionSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            ProjectTheme {
                val loginUiState by loginViewModel.uiState.collectAsState()
                val signUpUiState by signUpViewModel.uiState.collectAsState()
                var authDestination by remember { mutableStateOf(AuthDestination.Login) }
                var mainDestination by remember { mutableStateOf(MainDestination.Home) }
                var isGuestMode by remember { mutableStateOf(false) }
                var selectedPlace by remember { mutableStateOf<FilteredPlace?>(null) }
                val place = selectedPlace

                LaunchedEffect(signUpUiState.signupCompleted) {
                    if (signUpUiState.signupCompleted) {
                        authDestination = AuthDestination.Login
                        signUpViewModel.consumeSignupCompleted()
                    }
                }

                when {
                    loginUiState.isCheckingSession -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }

                    loginUiState.user == null && !isGuestMode -> {
                        when (authDestination) {
                            AuthDestination.Login -> LoginScreen(
                                uiState = loginUiState,
                                onBackClick = {},
                                onLoginClick = loginViewModel::login,
                                onNavigateToSignUp = { authDestination = AuthDestination.SignUp },
                                onGuestLoginClick = {
                                    isGuestMode = true
                                    mainDestination = MainDestination.Home
                                    selectedPlace = null
                                },
                                onKakaoLoginClick = {
                                    UserApiClient.instance.loginWithKakao(this@MainActivity) { token, error ->
                                        when {
                                            error != null -> {
                                                loginViewModel.showMessage("카카오 로그인에 실패했습니다.")
                                            }

                                            token != null -> {
                                                loginViewModel.kakaoLogin(token.accessToken)
                                            }
                                        }
                                    }
                                },
                                onGoogleLoginClick = {
                                    loginViewModel.showMessage("구글 SDK 토큰 연결 후 로그인할 수 있습니다.")
                                },
                                onMessageShown = loginViewModel::clearMessage,
                            )

                            AuthDestination.SignUp -> SignUpScreen(
                                uiState = signUpUiState,
                                onBackClick = { authDestination = AuthDestination.Login },
                                onSendCodeClick = signUpViewModel::sendCode,
                                onVerifyCodeClick = signUpViewModel::verifyCode,
                                onSignUpClick = signUpViewModel::signUp,
                                onMessageShown = signUpViewModel::clearMessage,
                            )
                        }
                    }

                    mainDestination == MainDestination.Home -> {
                        HomeScreen(
                            onNavigateToRegionSearch = {
                                selectedPlace = null
                                mainDestination = MainDestination.RegionSearch
                            },
                        )
                    }

                    place == null -> {
                        RegionSearchScreen(
                            viewModel = regionSearchViewModel,
                            onRegionPlaceClick = { selectedPlace = it },
                            onTitleClick = {
                                selectedPlace = null
                                mainDestination = MainDestination.Home
                            },
                        )
                    }

                    else -> {
                        RegionDetailScreen(
                            place = place,
                            onBackClick = { selectedPlace = null },
                        )
                    }
                }
            }
        }
    }

    private enum class AuthDestination {
        Login,
        SignUp,
    }

    private enum class MainDestination {
        Home,
        RegionSearch,
    }
}
