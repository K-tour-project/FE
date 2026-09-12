package com.everytrip.app

import android.os.Bundle
import android.content.MutableContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import com.everytrip.app.core.designsystem.component.NetworkErrorDialog
import com.everytrip.app.core.designsystem.component.AppBottomNavigationBar
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchScreen
import com.everytrip.app.feature.auth.presentation.login.LoginScreen
import com.everytrip.app.feature.auth.presentation.login.LoginViewModel
import com.everytrip.app.feature.auth.presentation.login.SessionCheckState
import com.everytrip.app.feature.auth.presentation.signup.SignUpScreen
import com.everytrip.app.feature.auth.presentation.signup.SignUpViewModel
import com.everytrip.app.feature.home.presentation.HomeScreen
import com.everytrip.app.feature.chatbot.presentation.AiChatbotScreen
import com.everytrip.app.feature.mypage.presentation.MyPageScreen
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

                LaunchedEffect(signUpUiState.signupCompleted) {
                    if (signUpUiState.signupCompleted) {
                        authDestination = AuthDestination.Login
                        signUpViewModel.consumeSignupCompleted()
                    }
                }

                when {
                    loginUiState.sessionCheckState == SessionCheckState.Checking -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }

                    loginUiState.sessionCheckState == SessionCheckState.RetryableError -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            NetworkErrorDialog(
                                onExitClick = this@MainActivity::finishAndRemoveTask,
                                onRetryClick = loginViewModel::checkExistingSession,
                            )
                        }
                    }

                    loginUiState.sessionCheckState == SessionCheckState.Unauthenticated &&
                        !isGuestMode -> {
                        when (authDestination) {
                            AuthDestination.Login -> LoginScreen(
                                uiState = loginUiState,
                                onBackClick = {},
                                onLoginClick = loginViewModel::login,
                                onNavigateToSignUp = { authDestination = AuthDestination.SignUp },
                                onGuestLoginClick = {
                                    isGuestMode = true
                                    mainDestination = MainDestination.Home
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
                                    startGoogleLogin()
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

                    else -> {
                        Scaffold(
                            bottomBar = {
                                AppBottomNavigationBar(
                                    selectedIndex = mainDestination.ordinal,
                                    onItemSelected = { index ->
                                        mainDestination = MainDestination.entries[index]
                                    },
                                )
                            },
                        ) { innerPadding ->
                            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                                when (mainDestination) {
                                    MainDestination.Home -> HomeScreen(
                                        onNavigateToSearch = {
                                            mainDestination = MainDestination.ArtworkSearch
                                        },
                                        onNavigateToRegionSearch = {
                                            mainDestination = MainDestination.RegionSearch
                                        },
                                    )
                                    MainDestination.Chatbot -> AiChatbotScreen()
                                    MainDestination.ArtworkSearch -> ArtworkSearchScreen()
                                    MainDestination.RegionSearch -> RegionSearchScreen(
                                        viewModel = regionSearchViewModel,
                                        onTitleClick = {
                                            mainDestination = MainDestination.Home
                                        },
                                    )
                                    MainDestination.MyPage -> MyPageScreen()
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun startGoogleLogin() {
        if (loginViewModel.uiState.value.isLoading) return
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) {
            loginViewModel.showMessage("구글 로그인 설정이 필요합니다.")
            return
        }
        loginViewModel.setGoogleSignInLoading(true)
        lifecycleScope.launch {
            val idToken = try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
                    .build()
                val credential = CredentialManager.create(this@MainActivity).getCredential(
                    context = MutableContextWrapper(this@MainActivity),
                    request = request,
                ).credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    loginViewModel.showMessage("구글 로그인 응답을 확인할 수 없습니다.")
                    return@launch
                }
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (_: GetCredentialCancellationException) {
                return@launch
            } catch (_: NoCredentialException) {
                loginViewModel.showMessage("구글 계정을 확인한 뒤 다시 시도해 주세요.")
                return@launch
            } catch (_: GetCredentialException) {
                loginViewModel.showMessage("구글 로그인에 실패했습니다. 다시 시도해 주세요.")
                return@launch
            } catch (_: GoogleIdTokenParsingException) {
                loginViewModel.showMessage("구글 로그인 토큰을 확인할 수 없습니다.")
                return@launch
            } finally {
                loginViewModel.setGoogleSignInLoading(false)
            }
            loginViewModel.googleLogin(idToken)
        }
    }

    private enum class AuthDestination {
        Login,
        SignUp,
    }

    private enum class MainDestination {
        Home,
        Chatbot,
        ArtworkSearch,
        RegionSearch,
        MyPage,
    }
}
