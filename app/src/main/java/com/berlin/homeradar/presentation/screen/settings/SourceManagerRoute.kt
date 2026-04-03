package com.berlin.homeradar.presentation.screen.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun SourceManagerRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch { writeText(context, uri, json) }
        }
        pendingExportJson = null
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val content = readText(context, uri)
                if (content != null) {
                    viewModel.importBackup(content) {}
                }
            }
        }
    }

    SourceManagerScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onSourceEnabledChanged = viewModel::onSourceEnabledChanged,
        onEnableAll = viewModel::enableAllSupportedSources,
        onDisableAll = viewModel::disableAllSupportedSources,
        onMoveSourceUp = { viewModel.moveSource(it, true) },
        onMoveSourceDown = { viewModel.moveSource(it, false) },
        onAddCustomSource = viewModel::addCustomSource,
        onRemoveCustomSource = viewModel::removeCustomSource,
        onTestSource = viewModel::testSource,
        onExportBackup = {
            viewModel.exportBackup { json ->
                pendingExportJson = json
                createDocument.launch("berlin-home-radar-backup.json")
            }
        },
        onImportBackup = { openDocument.launch(arrayOf("application/json", "text/plain")) },
    )
}

private fun writeText(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) }
}

private fun readText(context: Context, uri: Uri): String? {
    return context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
}
