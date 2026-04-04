package com.berlin.homeradar.presentation.screen.settings

import com.berlin.homeradar.R
import com.berlin.homeradar.data.config.RemoteConfigManager
import com.berlin.homeradar.data.preferences.AppSettingsApplier
import com.berlin.homeradar.data.source.SourceHealthMonitor
import com.berlin.homeradar.data.sync.SyncScheduler
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.RemoteConfigInfo
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceHealth
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import com.berlin.homeradar.domain.usecase.ObserveSyncInfoUseCase
import com.berlin.homeradar.domain.usecase.UpdateSyncSettingsUseCase
import com.berlin.homeradar.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val syncInfoFlow = MutableStateFlow(SyncInfo())
    private val sourceStatusesFlow = MutableStateFlow(emptyMap<String, SourceHealth>())
    private val sourceMetricsFlow = MutableStateFlow(emptyMap<String, SourceReliabilityMetrics>())
    private val remoteConfigInfoFlow = MutableStateFlow(RemoteConfigInfo())

    private val updateSyncSettingsUseCase: UpdateSyncSettingsUseCase = mock()
    private val syncScheduler: SyncScheduler = mock()
    private val appSettingsApplier: AppSettingsApplier = mock()
    private val sourceHealthMonitor: SourceHealthMonitor = mock {
        on { statuses } doReturn sourceStatusesFlow
    }
    private val remoteConfigManager: RemoteConfigManager = mock {
        on { info } doReturn remoteConfigInfoFlow
    }
    private val repository: HousingRepository = mock {
        on { getKnownSources() } doReturn emptyList()
        on { observeSyncInfo() } doReturn syncInfoFlow
        on { observeSourceReliabilityMetrics() } doReturn sourceMetricsFlow
    }

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        viewModel = buildViewModel()
    }

    @Test
    fun importBackup_emitsEmptyFileMessage_andSkipsRepositoryCall() = runTest {
        val event = async { viewModel.messages.first() }

        viewModel.importBackup("   ")
        advanceUntilIdle()

        assertEquals(R.string.backup_import_invalid_empty, event.await().resId)
        verify(updateSyncSettingsUseCase, never()).importBackupJson(any())
    }

    @Test
    fun importBackup_emitsInvalidFormatMessage_whenKeysAreMissing() = runTest {
        val event = async { viewModel.messages.first() }

        viewModel.importBackup("""{"foo":"bar"}""")
        advanceUntilIdle()

        assertEquals(R.string.backup_import_invalid_format, event.await().resId)
        verify(updateSyncSettingsUseCase, never()).importBackupJson(any())
    }

    @Test
    fun importBackup_appliesImportedSettings_andEmitsSuccess() = runTest {
        val validBackup = """
            {
              "backgroundSyncEnabled": true,
              "remoteSourceEnabled": true,
              "appLanguage": "de",
              "themeMode": "dark",
              "syncInterval": "1h",
              "enabledSourceIds": ["bundled-json"],
              "customSources": [],
              "sourceOrder": ["bundled-json"]
            }
        """.trimIndent()
        val importedInfo = SyncInfo(
            backgroundSyncEnabled = true,
            remoteSourceEnabled = true,
            appLanguage = AppLanguage.GERMAN,
            themeMode = ThemeMode.DARK,
            syncInterval = SyncIntervalOption.HOUR_1,
            enabledSourceIds = setOf("bundled-json"),
            sourceOrder = listOf("bundled-json"),
        )
        whenever(updateSyncSettingsUseCase.importBackupJson(validBackup)).doAnswer {
            syncInfoFlow.value = importedInfo
            Result.success(Unit)
        }
        val event = async { viewModel.messages.first() }

        viewModel.importBackup(validBackup)
        advanceUntilIdle()

        assertEquals(R.string.backup_import_success, event.await().resId)
        verify(appSettingsApplier).applyLanguage(AppLanguage.GERMAN)
        verify(appSettingsApplier).applyTheme(ThemeMode.DARK)
        verify(syncScheduler).schedulePeriodicSync(SyncIntervalOption.HOUR_1)
        assertFalse(viewModel.uiState.value.isImporting)
    }

    @Test
    fun importBackup_emitsFailure_whenRepositoryRejectsBackup() = runTest {
        val validBackup = """
            {
              "backgroundSyncEnabled": true,
              "remoteSourceEnabled": false,
              "appLanguage": "system",
              "themeMode": "system",
              "syncInterval": "15m",
              "enabledSourceIds": ["bundled-json"],
              "customSources": [],
              "sourceOrder": ["bundled-json"]
            }
        """.trimIndent()
        whenever(updateSyncSettingsUseCase.importBackupJson(validBackup)).doReturn(
            Result.failure(IllegalArgumentException("bad backup")),
        )
        val event = async { viewModel.messages.first() }

        viewModel.importBackup(validBackup)
        advanceUntilIdle()

        assertEquals(R.string.backup_import_failed, event.await().resId)
        verify(appSettingsApplier, never()).applyLanguage(any())
        verify(syncScheduler, never()).schedulePeriodicSync(any())
    }

    @Test
    fun exportBackup_emitsFailure_andSkipsCallback_whenExportThrows() = runTest {
        whenever(updateSyncSettingsUseCase.exportBackupJson()).doAnswer { throw IllegalStateException("boom") }
        val event = async { viewModel.messages.first() }
        var callbackInvoked = false

        viewModel.exportBackup { callbackInvoked = true }
        advanceUntilIdle()

        assertEquals(R.string.backup_export_failed, event.await().resId)
        assertFalse(callbackInvoked)
    }

    @Test
    fun manualRefresh_emitsStartedMessage_andEnqueuesWorker() = runTest {
        val event = async { viewModel.messages.first() }

        viewModel.manualRefresh()
        advanceUntilIdle()

        assertEquals(R.string.manual_refresh_started, event.await().resId)
        verify(syncScheduler).manualRefresh()
    }

    @Test
    fun testSource_emitsSuccessMessage_forHealthySource() = runTest {
        val source = SourceDefinition(
            id = "bundled-json",
            displayName = "Bundled JSON",
            websiteUrl = "https://example.com",
            supportsAutomatedSync = true,
        )
        whenever(repository.getKnownSources()).thenReturn(listOf(source))
        viewModel = buildViewModel()
        whenever(sourceHealthMonitor.testSource(source)).thenReturn(Result.success(Unit))
        val event = async { viewModel.messages.first() }

        viewModel.testSource("bundled-json")
        advanceUntilIdle()

        val message = event.await()
        assertEquals(R.string.source_test_success, message.resId)
        assertEquals(listOf("Bundled JSON"), message.args)
    }

    @Test
    fun testSource_emitsManualOnlyMessage_forCatalogSource() = runTest {
        val source = SourceDefinition(
            id = "catalog-source",
            displayName = "Catalog Source",
            websiteUrl = "https://example.com/catalog",
            supportsAutomatedSync = false,
        )
        whenever(repository.getKnownSources()).thenReturn(listOf(source))
        viewModel = buildViewModel()
        whenever(sourceHealthMonitor.testSource(source)).thenReturn(Result.success(Unit))
        val event = async { viewModel.messages.first() }

        viewModel.testSource("catalog-source")
        advanceUntilIdle()

        val message = event.await()
        assertEquals(R.string.source_test_manual_only, message.resId)
        assertEquals(listOf("Catalog Source"), message.args)
    }

    private fun buildViewModel(): SettingsViewModel = SettingsViewModel(
        observeSyncInfoUseCase = ObserveSyncInfoUseCase(repository),
        updateSyncSettingsUseCase = updateSyncSettingsUseCase,
        syncScheduler = syncScheduler,
        appSettingsApplier = appSettingsApplier,
        sourceHealthMonitor = sourceHealthMonitor,
        remoteConfigManager = remoteConfigManager,
        repository = repository,
    )
}
