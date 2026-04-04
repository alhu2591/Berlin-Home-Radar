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
import androidx.compose.ui.platform.testTag
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
internal fun SourceManagerHeroCard(
    isImporting: Boolean,
    sourceMetrics: Map<String, SourceReliabilityMetrics>,
    onEnableAll: () -> Unit,
    onDisableAll: () -> Unit,
    onOpenAddSource: () -> Unit,
    onImportBackup: () -> Unit,
    onExportBackup: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.source_manager_hero_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.source_health_summary),
                style = MaterialTheme.typography.bodyMedium,
            )
            ReliabilityDashboardCard(sourceMetrics = sourceMetrics)
            if (isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.backup_import_progress),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(SourceManagerTestTags.BACKUP_IMPORT_PROGRESS),
                )
            }
            SourceManagerBulkActions(
                onEnableAll = onEnableAll,
                onDisableAll = onDisableAll,
            )
            SourceManagerUtilityActions(
                isImporting = isImporting,
                onOpenAddSource = onOpenAddSource,
                onImportBackup = onImportBackup,
                onExportBackup = onExportBackup,
            )
        }
    }
}

@Composable
internal fun ReliabilityDashboardCard(
    sourceMetrics: Map<String, SourceReliabilityMetrics>,
) {
    val healthyCount = sourceMetrics.values.count { it.successRatePercent >= 60 || it.totalAttempts == 0 }
    val needsAttentionCount = sourceMetrics.values.count { it.totalAttempts > 0 && it.successRatePercent < 60 }
    val zeroResultAlerts = sourceMetrics.values.count { it.hasZeroResultsAnomaly }
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(R.string.source_metrics_dashboard_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.source_metrics_dashboard_subtitle),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(
                    R.string.source_metrics_dashboard_summary,
                    healthyCount,
                    needsAttentionCount,
                    zeroResultAlerts,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun SourceManagerBulkActions(
    onEnableAll: () -> Unit,
    onDisableAll: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onEnableAll) { Text(stringResource(R.string.enable_all_label)) }
        OutlinedButton(onClick = onDisableAll) { Text(stringResource(R.string.disable_all_label)) }
    }
}

@Composable
internal fun SourceManagerUtilityActions(
    isImporting: Boolean,
    onOpenAddSource: () -> Unit,
    onImportBackup: () -> Unit,
    onExportBackup: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onOpenAddSource) { Text(stringResource(R.string.settings_add_source)) }
        OutlinedButton(onClick = onImportBackup, enabled = !isImporting) { Text(stringResource(R.string.import_backup_label)) }
        OutlinedButton(onClick = onExportBackup, enabled = !isImporting) { Text(stringResource(R.string.export_backup_label)) }
    }
}
