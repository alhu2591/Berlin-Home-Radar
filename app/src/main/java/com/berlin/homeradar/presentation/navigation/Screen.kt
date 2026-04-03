package com.berlin.homeradar.presentation.navigation

sealed class Screen(val route: String) {
    data object Listings : Screen("listings")
    data object Settings : Screen("settings")
}
