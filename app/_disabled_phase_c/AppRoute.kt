package com.replysense.app.ui.nav

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Analysis : AppRoute("analysis")
    data object Review : AppRoute("review")
}
