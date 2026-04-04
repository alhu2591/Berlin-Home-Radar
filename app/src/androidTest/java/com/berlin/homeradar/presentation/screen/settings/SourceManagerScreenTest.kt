package com.berlin.homeradar.presentation.screen.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SourceManagerScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sourceManagerScreen_showsImportProgress_whenBackupImportIsRunning() {
        composeRule.setContent {
            SourceManagerScreen(
                uiState = MutableStateFlow(SettingsUiState(isImporting = true)),
                snackbarHostState = SnackbarHostState(),
                onBack = {},
                onSourceEnabledChanged = { _, _ -> },
                onEnableAll = {},
                onDisableAll = {},
                onMoveSourceUp = {},
                onMoveSourceDown = {},
                onAddCustomSource = { _, _, _ -> },
                onRemoveCustomSource = {},
                onTestSource = {},
                onExportBackup = {},
                onImportBackup = {},
            )
        }

        composeRule.onNodeWithTag(SourceManagerTestTags.BACKUP_IMPORT_PROGRESS).assertIsDisplayed()
    }
}
