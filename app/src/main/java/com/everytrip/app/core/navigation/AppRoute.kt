package com.everytrip.app.core.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object SignUp : AppRoute("signup")
    data object PrivacyPolicy : AppRoute("privacy-policy")
    data object TermsOfService : AppRoute("terms-of-service")
    data object Home : AppRoute("home")
    data object Chatbot : AppRoute("chatbot")
    data object ArtworkSearch : AppRoute("artwork-search")
    data object RegionSearch : AppRoute("region-search")
    data object MyPage : AppRoute("my-page")
    data object Settings : AppRoute("settings")
    data object ArtworkDetail : AppRoute("artwork-detail/{productId}") {
        fun createRoute(productId: Int) = "artwork-detail/$productId"
    }

    companion object {
        val bottomRoutes = listOf(Home, Chatbot, ArtworkSearch, RegionSearch, MyPage)
    }
}
