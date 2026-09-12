package com.everytrip.app.core.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object SignUp : AppRoute("signup")
    data object Home : AppRoute("home")
    data object Chatbot : AppRoute("chatbot")
    data object ArtworkSearch : AppRoute("artwork-search")
    data object RegionSearch : AppRoute("region-search")
    data object MyPage : AppRoute("my-page")

    companion object {
        val bottomRoutes = listOf(Home, Chatbot, ArtworkSearch, RegionSearch, MyPage)
    }
}
