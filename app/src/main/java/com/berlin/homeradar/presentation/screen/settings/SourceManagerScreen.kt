package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SourceType
import com.berlin.homeradar.presentation.util.formatTimestamp
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagerScreen(
    uiState: StateFlow<SettingsUiState>,
    snackbarHostState: SnackbarHostState,
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
    val state by uiState.collectAsStateWithLifecycle()
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SourceManagerContent(
            state = state,
            padding = padding,
            onEnableAll = onEnableAll,
            onDisableAll = onDisableAll,
            onOpenAddSource = { showAddSourceDialog = true },
            onImportBackup = onImportBackup,
            onExportBackup = onExportBackup,
            onSourceEnabledChanged = onSourceEnabledChanged,
            onMoveSourceUp = onMoveSourceUp,
            onMoveSourceDown = onMoveSourceDown,
            onOpenSource = { websiteUrl -> uriHandler.openUri(websiteUrl) },
            onTestSource = onTestSource,
            onRemoveCustomSource = onRemoveCustomSource,
        )
    }
}

@Composable
private fun SourceManagerContent(
    state: SettingsUiState,
    padding: PaddingValues,
    onEnableAll: () -> Unit,
    onDisableAll: () -> Unit,
    onOpenAddSource: () -> Unit,
    onImportBackup: () -> Unit,
    onExportBackup: () -> Unit,
    onSourceEnabledChanged: (String, Boolean) -> Unit,
    onMoveSourceUp: (String) -> Unit,
    onMoveSourceDown: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onTestSource: (String) -> Unit,
    onRemoveCustomSource: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        val wideLayout = maxWidth >= 1080.dp
        if (wideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.width(360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SourceManagerHeroCard(
                        isImporting = state.isImporting,
                        sourceMetrics = state.sourceMetrics,
                        onEnableAll = onEnableAll,
                        onDisableAll = onDisableAll,
                        onOpenAddSource = onOpenAddSource,
                        onImportBackup = onImportBackup,
                        onExportBackup = onExportBackup,
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(state.sources, key = { _, item -> item.id }) { index, source ->
                        SourceManagerItem(
                            index = index,
                            source = source,
                            enabled = source.id in state.enabledSourceIds,
                            health = state.sourceHealth[source.id],
                            metrics = state.sourceMetrics[source.id],
                            canMoveUp = index > 0,
                            canMoveDown = index < state.sources.lastIndex,
                            onEnabledChanged = { enabled -> onSourceEnabledChanged(source.id, enabled) },
                            onMoveUp = { onMoveSourceUp(source.id) },
                            onMoveDown = { onMoveSourceDown(source.id) },
                            onOpen = { onOpenSource(source.websiteUrl) },
                            onTest = { onTestSource(source.id) },
                            onRemove = if (source.isUserAdded) ({ onRemoveCustomSource(source.id) }) else null,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SourceManagerHeroCard(
                        isImporting = state.isImporting,
                        sourceMetrics = state.sourceMetrics,
                        onEnableAll = onEnableAll,
                        onDisableAll = onDisableAll,
                        onOpenAddSource = onOpenAddSource,
                        onImportBackup = onImportBackup,
                        onExportBackup = onExportBackup,
                    )
                }

                itemsIndexed(state.sources, key = { _, item -> item.id }) { index, source ->
                    SourceManagerItem(
                        index = index,
                        source = source,
                        enabled = source.id in state.enabledSourceIds,
                        health = state.sourceHealth[source.id],
                        metrics = state.sourceMetrics[source.id],
                        canMoveUp = index > 0,
                        canMoveDown = index < state.sources.lastIndex,
                        onEnabledChanged = { enabled -> onSourceEnabledChanged(source.id, enabled) },
                        onMoveUp = { onMoveSourceUp(source.id) },
                        onMoveDown = { onMoveSourceDown(source.id) },
                        onOpen = { onOpenSource(source.websiteUrl) },
                        onTest = { onTestSource(source.id) },
                        onRemove = if (source.isUserAdded) ({ onRemoveCustomSource(source.id) }) else null,
                    )
                }
            }
        }
    }
}

