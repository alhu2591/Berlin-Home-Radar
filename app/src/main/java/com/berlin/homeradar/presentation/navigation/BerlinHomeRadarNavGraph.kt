package com.berlin.homeradar.presentation.navigation

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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.berlin.homeradar.presentation.screen.listings.ListingsRoute
import com.berlin.homeradar.presentation.screen.settings.SettingsRoute

@Composable
fun BerlinHomeRadarNavGraph() {
    val navController = rememberNavController()
    val items = listOf(Screen.Listings, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                items.forEach { screen ->
                    val selected = navBackStackEntry?.destination?.hierarchy?.any {
                        it.route == screen.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Listings -> Icons.Outlined.Home
                                    Screen.Settings -> Icons.Outlined.Settings
                                },
                                contentDescription = screen.route,
                            )
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    Screen.Listings -> "Listings"
                                    Screen.Settings -> "Settings"
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Listings.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Listings.route) { ListingsRoute() }
            composable(Screen.Settings.route) { SettingsRoute() }
        }
    }
}
