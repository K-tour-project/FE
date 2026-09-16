package com.everytrip.app

import android.content.MutableContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.everytrip.app.core.navigation.AppNavHost
import com.everytrip.app.feature.auth.presentation.login.LoginViewModel
import com.everytrip.app.feature.auth.presentation.signup.SignUpViewModel
import com.everytrip.app.feature.chatbot.presentation.ChatViewModel
import com.everytrip.app.feature.mypage.presentation.FavoriteViewModel
import com.everytrip.app.feature.region.presentation.search.RegionSearchViewModel
import com.everytrip.app.ui.theme.ProjectTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()
    private val regionSearchViewModel: RegionSearchViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            ProjectTheme {
                AppNavHost(
                    loginViewModel = loginViewModel,
                    signUpViewModel = signUpViewModel,
                    regionSearchViewModel = regionSearchViewModel,
                    chatViewModel = chatViewModel,
                    favoriteViewModel = favoriteViewModel,
                    onKakaoLoginClick = ::startKakaoLogin,
                    onGoogleLoginClick = ::startGoogleLogin,
                    onExitClick = ::finishAndRemoveTask,
                )
            }
        }
    }

    private fun startKakaoLogin() {
        UserApiClient.instance.loginWithKakao(this) { token, error ->
            when {
                error != null -> loginViewModel.showMessage("카카오 로그인에 실패했습니다.")
                token != null -> loginViewModel.kakaoLogin(token.accessToken)
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
                loginViewModel.showMessage("구글 계정을 확인하고 다시 시도해 주세요.")
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
}
