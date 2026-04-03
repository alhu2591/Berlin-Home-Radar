package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceHealth
import com.berlin.homeradar.domain.model.SourceHealthStatus
import com.berlin.homeradar.domain.model.SourceType
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagerScreen(
    uiState: StateFlow<SettingsUiState>,
    onBack: () -> Unit,
    onSourceEnabledChanged: (String, Boolean) -> Unit,
    onEnableAll: () -> Unit,
    onDisableAll: () -> Unit,
    onMoveSourceUp: (String) -> Unit,
    onMoveSourceDown: (String) -> Unit,
    onAddCustomSource: (String, String, String) -> Unit,
    onRemoveCustomSource: (String) -> Unit,
    onTestSource: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
) {
    val state by uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    var showAddSourceDialog by remember { mutableStateOf(false) }

    if (showAddSourceDialog) {
        AddSourceDialog(
            onDismiss = { showAddSourceDialog = false },
            onConfirm = { name, url, description ->
                onAddCustomSource(name, url, description)
                showAddSourceDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_manage_sources_button)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Source control center", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "The app keeps going even when one source fails. Test sources, disable noisy feeds, reorder priority, and remove custom sources here.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onEnableAll) { Text(stringResource(R.string.enable_all_label)) }
                            OutlinedButton(onClick = onDisableAll) { Text(stringResource(R.string.disable_all_label)) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showAddSourceDialog = true }) { Text(stringResource(R.string.settings_add_source)) }
                            OutlinedButton(onClick = onImportBackup) { Text(stringResource(R.string.import_backup_label)) }
                            OutlinedButton(onClick = onExportBackup) { Text(stringResource(R.string.export_backup_label)) }
                        }
                    }
                }
            }

            itemsIndexed(state.sources, key = { _, item -> item.id }) { index, source ->
                SourceManagerItem(
                    index = index,
                    source = source,
                    enabled = source.id in state.enabledSourceIds,
                    health = state.sourceHealth[source.id],
                    canMoveUp = index > 0,
                    canMoveDown = index < state.sources.lastIndex,
                    onEnabledChanged = { enabled -> onSourceEnabledChanged(source.id, enabled) },
                    onMoveUp = { onMoveSourceUp(source.id) },
                    onMoveDown = { onMoveSourceDown(source.id) },
                    onOpen = { uriHandler.openUri(source.websiteUrl) },
                    onTest = { onTestSource(source.id) },
                    onRemove = if (source.isUserAdded) ({ onRemoveCustomSource(source.id) }) else null,
                )
            }
        }
    }
}

@Composable
private fun SourceManagerItem(
    index: Int,
    source: SourceDefinition,
    enabled: Boolean,
    health: SourceHealth?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onOpen: () -> Unit,
    onTest: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    Card {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ListItem(
                headlineContent = { Text("${index + 1}. ${source.displayName}") },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(source.description)
                        Text(source.websiteUrl, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ElevatedAssistChip(
                                onClick = {},
                                label = { Text(sourceTypeText(source.sourceType)) },
                            )
                            ElevatedAssistChip(
                                onClick = {},
                                label = { Text(healthText(health?.status ?: defaultStatusFor(source))) },
                            )
                        }
                        if (!health?.message.isNullOrBlank()) {
                            Text(health?.message.orEmpty(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                trailingContent = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChanged,
                        enabled = source.supportsAutomatedSync || source.isUserAdded,
                    )
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onOpen) {
                    Icon(Icons.Outlined.OpenInBrowser, contentDescription = null)
                    Text(" Open")
                }
                OutlinedButton(
                    onClick = onTest,
                    enabled = source.supportsAutomatedSync || source.isUserAdded,
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Text(" Test")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Outlined.ArrowUpward, contentDescription = null)
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Outlined.ArrowDownward, contentDescription = null)
                }
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_add_source)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_add_source_hint), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.source_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.source_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.source_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, url, description) }) {
                Text(stringResource(R.string.add_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label))
            }
        },
    )
}

@Composable
private fun sourceTypeText(type: SourceType): String = when (type) {
    SourceType.API -> "API"
    SourceType.HTML -> "HTML"
    SourceType.WEBVIEW -> "WebView"
    SourceType.CATALOG -> "Catalog"
}

@Composable
private fun healthText(status: SourceHealthStatus): String = when (status) {
    SourceHealthStatus.IDLE -> "Idle"
    SourceHealthStatus.CHECKING -> "Checking"
    SourceHealthStatus.SUCCESS -> "Healthy"
    SourceHealthStatus.FAILED -> "Failed"
    SourceHealthStatus.UNSUPPORTED -> "Catalog only"
}

private fun defaultStatusFor(source: SourceDefinition): SourceHealthStatus = when {
    source.supportsAutomatedSync -> SourceHealthStatus.IDLE
    source.sourceType == SourceType.CATALOG -> SourceHealthStatus.UNSUPPORTED
    else -> SourceHealthStatus.IDLE
}
