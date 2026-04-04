package com.berlin.homeradar.presentation.screen.savedsearches

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.berlin.homeradar.R
import kotlinx.coroutines.launch

@Composable
fun SavedSearchesRoute(
    onBack: () -> Unit,
    viewModel: SavedSearchesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingSearchId by remember { mutableStateOf<String?>(null) }

    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val searchId = pendingSearchId
        pendingSearchId = null
        if (granted && searchId != null) {
            viewModel.setAlertEnabled(searchId, true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.notification_permission_required_for_alerts),
                )
            }
        }
    }

    fun handleAlertToggle(searchId: String, enabled: Boolean) {
        if (!enabled) {
            viewModel.setAlertEnabled(searchId, false)
            return
        }
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.setAlertEnabled(searchId, true)
            return
        }
        pendingSearchId = searchId
        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    SavedSearchesScreen(
        uiState = viewModel.uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onDeleteSearch = viewModel::delete,
        onAlertEnabledChanged = ::handleAlertToggle,
    )
}
