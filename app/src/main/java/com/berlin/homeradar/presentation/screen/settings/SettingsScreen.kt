package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(
    uiState: StateFlow<SettingsUiState>,
    onBackgroundSyncChanged: (Boolean) -> Unit,
    onRemoteSourceChanged: (Boolean) -> Unit,
    onManualRefresh: () -> Unit,
) {
    val state by uiState.collectAsState()

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Sync controls",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Periodic sync is scheduled with WorkManager every 15 minutes because Android does not guarantee more frequent execution.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = onManualRefresh) {
                            Text("Run manual refresh")
                        }
                    }
                }
            }
            item {
                Card {
                    Column {
                        ListItem(
                            headlineContent = { Text("Background sync") },
                            supportingContent = { Text("Enable or disable periodic WorkManager sync") },
                            trailingContent = {
                                Switch(
                                    checked = state.backgroundSyncEnabled,
                                    onCheckedChange = onBackgroundSyncChanged,
                                )
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Optional remote source") },
                            supportingContent = { Text("Disabled by default. Safe to enable only after you expose your own JSON feed.") },
                            trailingContent = {
                                Switch(
                                    checked = state.remoteSourceEnabled,
                                    onCheckedChange = onRemoteSourceChanged,
                                )
                            }
                        )
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Last successful sync: ${state.lastSuccessfulSyncText}")
                        Text("Last attempt: ${state.lastAttemptText}")
                        Text("Last error: ${state.lastErrorMessage ?: "None"}")
                    }
                }
            }
        }
    }
}
