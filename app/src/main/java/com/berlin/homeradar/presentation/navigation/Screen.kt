package com.berlin.homeradar.presentation.navigation

sealed class Screen(val route: String) {
    data object Listings : Screen("listings")
    data object Settings : Screen("settings")
    data object SourceManager : Screen("source_manager")
    data object ListingDetails : Screen("listing_details/{listingId}") {
        fun createRoute(listingId: Long) = "listing_details/$listingId"
    }
    data object SavedSearches : Screen("saved_searches")
    data object InAppBrowser : Screen("in_app_browser/{title}/{url}") {
        fun createRoute(title: String, url: String): String =
            "in_app_browser/${android.net.Uri.encode(title)}/${android.net.Uri.encode(url)}"
    }
}
