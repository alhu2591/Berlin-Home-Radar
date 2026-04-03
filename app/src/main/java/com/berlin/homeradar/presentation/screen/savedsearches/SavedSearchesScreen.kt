package com.berlin.homeradar.presentation.screen.savedsearches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchesScreen(
    uiState: StateFlow<SavedSearchesUiState>,
    onBack: () -> Unit,
    onDeleteSearch: (String) -> Unit,
    onAlertEnabledChanged: (String, Boolean) -> Unit,
) {
    val state by uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved searches & local alerts") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = null) } },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.searches, key = { it.id }) { search ->
                Card {
                    ListItem(
                        headlineContent = { Text(search.name) },
                        supportingContent = { Text("Alerts are local only and evaluated on refresh inside the app.") },
                        trailingContent = {
                            Switch(
                                checked = search.alertsEnabled,
                                onCheckedChange = { onAlertEnabledChanged(search.id, it) },
                            )
                        }
                    )
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { onDeleteSearch(search.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
