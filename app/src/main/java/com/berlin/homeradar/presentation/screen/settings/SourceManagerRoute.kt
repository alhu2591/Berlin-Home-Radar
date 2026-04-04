package com.berlin.homeradar.presentation.screen.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.berlin.homeradar.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SourceManagerRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message.resolve(context))
        }
    }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                writeText(context, uri, json)
                    .onSuccess {
                        snackbarHostState.showSnackbar(context.getString(R.string.backup_export_success))
                    }
                    .onFailure {
                        snackbarHostState.showSnackbar(context.getString(R.string.backup_export_failed))
                    }
            }
        }
        pendingExportJson = null
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                readText(context, uri)
                    .onSuccess { content -> viewModel.importBackup(content) }
                    .onFailure {
                        snackbarHostState.showSnackbar(context.getString(R.string.backup_import_read_failed))
                    }
            }
        }
    }

    SourceManagerScreen(
        uiState = viewModel.uiState,
        snackbarHostState = snackbarHostState,
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

private suspend fun writeText(context: Context, uri: Uri, text: String): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) }
            ?: error("Could not open output stream")
    }
}

private suspend fun readText(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not open input stream")
    }
}
