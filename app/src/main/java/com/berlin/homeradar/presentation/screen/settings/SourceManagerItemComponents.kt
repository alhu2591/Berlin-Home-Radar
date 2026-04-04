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


@Composable
internal fun SourceManagerItem(
    index: Int,
    source: SourceDefinition,
    enabled: Boolean,
    health: SourceHealth?,
    metrics: SourceReliabilityMetrics?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onOpen: () -> Unit,
    onTest: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val currentStatus = health?.status ?: defaultStatusFor(source)
    val isToggleEnabled = source.supportsAutomatedSync || source.isUserAdded
    Card {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.source_position_format, index + 1, source.displayName)) },
                supportingContent = {
                    SourceDetails(
                        source = source,
                        health = health,
                        metrics = metrics,
                        currentStatus = currentStatus,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChanged,
                        enabled = isToggleEnabled,
                    )
                },
            )

            SourcePrimaryActionsRow(
                canTest = isToggleEnabled,
                onOpen = onOpen,
                onTest = onTest,
            )

            SourceReorderRow(
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onRemove = onRemove,
            )
        }
    }
}

@Composable
internal fun SourceDetails(
    source: SourceDefinition,
    health: SourceHealth?,
    metrics: SourceReliabilityMetrics?,
    currentStatus: SourceHealthStatus,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(source.description)
        Text(source.websiteUrl, style = MaterialTheme.typography.bodySmall)
        Text(
            text = stringResource(R.string.source_type_format, sourceTypeText(source.sourceType)),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(
                R.string.source_status_format,
                healthText(currentStatus),
                health?.message ?: source.description,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        SourceStatusChips(
            sourceType = source.sourceType,
            currentStatus = currentStatus,
        )
        metrics?.let { SourceMetricsSummarySection(metrics = it) }
        if (!health?.message.isNullOrBlank()) {
            Text(health?.message.orEmpty(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun SourceMetricsSummarySection(
    metrics: SourceReliabilityMetrics,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.source_metrics_success_rate, metrics.successRatePercent),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(R.string.source_metrics_average_items, metrics.averageItemCount.roundToInt().toString()),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(R.string.source_metrics_average_duration, metrics.averageDurationMillis.roundToInt().toString()),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(
                R.string.source_metrics_last_success,
                formatTimestamp(metrics.lastSuccessfulPullMillis),
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        if (metrics.consecutiveZeroItemPulls > 0) {
            Text(
                text = stringResource(R.string.source_metrics_zero_results, metrics.consecutiveZeroItemPulls),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun SourceStatusChips(
    sourceType: SourceType,
    currentStatus: SourceHealthStatus,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ElevatedAssistChip(
            onClick = {},
            label = { Text(sourceTypeText(sourceType)) },
        )
        ElevatedAssistChip(
            onClick = {},
            label = { Text(healthText(currentStatus)) },
        )
    }
}

@Composable
internal fun SourcePrimaryActionsRow(
    canTest: Boolean,
    onOpen: () -> Unit,
    onTest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onOpen) {
            Icon(Icons.Outlined.OpenInBrowser, contentDescription = stringResource(R.string.source_open_action))
            Text(stringResource(R.string.open_source_label))
        }
        OutlinedButton(
            onClick = onTest,
            enabled = canTest,
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.source_test_action))
            Text(stringResource(R.string.test_source_label))
        }
    }
}

@Composable
internal fun SourceReorderRow(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onMoveUp, enabled = canMoveUp) {
            Icon(Icons.Outlined.ArrowUpward, contentDescription = stringResource(R.string.source_move_up_action))
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown) {
            Icon(Icons.Outlined.ArrowDownward, contentDescription = stringResource(R.string.source_move_down_action))
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.source_delete_action))
            }
        }
    }
}
