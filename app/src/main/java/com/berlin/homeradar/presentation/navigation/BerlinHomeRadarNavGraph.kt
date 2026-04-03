package com.berlin.homeradar.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.berlin.homeradar.presentation.screen.webview.InAppBrowserScreen
import com.berlin.homeradar.R
import com.berlin.homeradar.presentation.screen.details.ListingDetailsRoute
import com.berlin.homeradar.presentation.screen.listings.ListingsRoute
import com.berlin.homeradar.presentation.screen.onboarding.OnboardingRoute
import com.berlin.homeradar.presentation.screen.savedsearches.SavedSearchesRoute
import com.berlin.homeradar.presentation.screen.settings.SettingsRoute
import com.berlin.homeradar.presentation.screen.settings.SourceManagerRoute
import com.berlin.homeradar.presentation.screen.onboarding.OnboardingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun BerlinHomeRadarNavGraph() {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted by onboardingViewModel.completed.collectAsState()
    val context = LocalContext.current
    val items = listOf(Screen.Listings, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                items.forEach { screen ->
                    val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Listings -> Icons.Outlined.Home
                                    else -> Icons.Outlined.Settings
                                },
                                contentDescription = screen.route,
                            )
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    Screen.Listings -> stringResource(R.string.nav_listings)
                                    else -> stringResource(R.string.nav_settings)
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = if (onboardingCompleted) Screen.Listings.route else Screen.Onboarding.route, modifier = Modifier.padding(padding)) {
            composable(Screen.Onboarding.route) {
                OnboardingRoute(
                    onFinished = {
                        navController.navigate(Screen.Listings.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Listings.route) {
                ListingsRoute(
                    onListingClick = { navController.navigate(Screen.ListingDetails.createRoute(it)) },
                    onSavedSearchesClick = { navController.navigate(Screen.SavedSearches.route) },
                )
            }
            composable(Screen.Settings.route) {
                SettingsRoute(onManageSources = { navController.navigate(Screen.SourceManager.route) })
            }
            composable(Screen.SourceManager.route) {
                SourceManagerRoute(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.ListingDetails.route,
                arguments = listOf(navArgument("listingId") { type = NavType.LongType }),
                deepLinks = listOf(navDeepLink { uriPattern = "berlinhomeradar://listing/{listingId}" })
            ) {
                ListingDetailsRoute(
                    onBack = { navController.popBackStack() },
                    onOpenBrowser = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    onOpenInApp = { title, url -> navController.navigate(Screen.InAppBrowser.createRoute(title, url)) },
                    onShare = { url ->
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, url)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    },
                )
            }
            composable(Screen.SavedSearches.route) {
                SavedSearchesRoute(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.InAppBrowser.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("url") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                InAppBrowserScreen(
                    title = backStackEntry.arguments?.getString("title").orEmpty(),
                    initialUrl = backStackEntry.arguments?.getString("url").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenExternal = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                )
            }
        }
    }
}
