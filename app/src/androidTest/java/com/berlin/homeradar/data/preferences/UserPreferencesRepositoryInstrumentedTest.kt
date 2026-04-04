package com.berlin.homeradar.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.ThemeMode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryInstrumentedTest {
    private lateinit var context: Context
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = newRepository()
    }

    @Test
    fun setRemoteSourceEnabled_updatesEnabledSourceIds() = runBlocking {
        repository.setRemoteSourceEnabled(true)
        var settings = repository.appSettingsSnapshot()
        assertTrue(settings.remoteSourceEnabled)
        assertTrue(SourceCatalog.RemoteJson.id in settings.enabledSourceIds)

        repository.setRemoteSourceEnabled(false)
        settings = repository.appSettingsSnapshot()
        assertFalse(settings.remoteSourceEnabled)
        assertFalse(SourceCatalog.RemoteJson.id in settings.enabledSourceIds)
    }

    @Test
    fun addAndRemoveCustomSource_updatesCatalogEnabledIdsAndOrder() = runBlocking {
        repository.addCustomSource(
            displayName = "My JSON Feed",
            websiteUrl = "https://example.com/feed.json",
            description = "",
        )

        var settings = repository.appSettingsSnapshot()
        val customSource = settings.customSources.single()
        assertEquals("custom-my-json-feed", customSource.id)
        assertEquals("My JSON Feed", customSource.displayName)
        assertTrue(customSource.supportsAutomatedSync)
        assertTrue(customSource.id in settings.enabledSourceIds)
        assertEquals(customSource.id, settings.sourceOrder.last())

        repository.removeCustomSource(customSource.id)

        settings = repository.appSettingsSnapshot()
        assertTrue(settings.customSources.isEmpty())
        assertFalse(customSource.id in settings.enabledSourceIds)
        assertFalse(customSource.id in settings.sourceOrder)
    }

    @Test
    fun exportBackupAndRestore_roundTripsAndNormalizesRemoteSource() = runBlocking {
        repository.setAppLanguage(AppLanguage.ARABIC)
        repository.setThemeMode(ThemeMode.DARK)
        repository.setBackgroundSyncEnabled(false)
        repository.setRemoteSourceEnabled(true)
        repository.addCustomSource(
            displayName = "Archive Feed",
            websiteUrl = "https://example.com/archive.json",
            description = "Custom archive",
        )
        repository.saveSearch(
            SavedSearch(
                id = "saved-1",
                name = "Affordable",
                filters = ListingFilterPreset(maxPrice = 1200),
            ),
        )

        val originalBackup = repository.exportBackupJson()
        val tamperedBackup = tamperBackupJson(originalBackup)

        val restoredRepository = newRepository()
        val result = restoredRepository.replaceAllFromBackup(tamperedBackup)
        assertTrue(result.isSuccess)

        val restored = restoredRepository.appSettingsSnapshot()
        assertEquals(AppLanguage.ARABIC, restored.language)
        assertEquals(ThemeMode.DARK, restored.themeMode)
        assertFalse(restored.backgroundSyncEnabled)
        assertFalse(restored.remoteSourceEnabled)
        assertFalse(SourceCatalog.RemoteJson.id in restored.enabledSourceIds)
        assertFalse("unsupported-id" in restored.enabledSourceIds)
        assertEquals(1, restored.customSources.size)
        assertEquals(1, restored.savedSearches.size)
        assertEquals("custom-archive-feed", restored.sourceOrder.first { it.startsWith("custom-") })
        assertEquals(1, restored.sourceOrder.count { it == "custom-archive-feed" })
    }

    @Test
    fun replaceAllFromBackup_returnsFailureForInvalidJsonAndLeavesDefaults() = runBlocking {
        val before = repository.appSettingsSnapshot()

        val result = repository.replaceAllFromBackup("not-valid-json")

        assertTrue(result.isFailure)
        assertEquals(before, repository.appSettingsSnapshot())
    }

    private fun newRepository(): UserPreferencesRepository =
        UserPreferencesRepository.createForTesting(
            context = context,
            fileName = "user-prefs-test-${System.nanoTime()}.preferences_pb",
        )

    private fun tamperBackupJson(originalBackup: String): String {
        val json = Json { prettyPrint = true }
        val root = json.parseToJsonElement(originalBackup).jsonObject
        val customSourceId = root["customSources"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("id")
            ?.jsonPrimitive
            ?.content
            ?: "custom-archive-feed"

        val tampered = buildJsonObject {
            root.forEach { (key, value) ->
                when (key) {
                    "remoteSourceEnabled" -> put(key, false)
                    "enabledSourceIds" -> put(
                        key,
                        buildJsonArray {
                            add(customSourceId)
                            add(SourceCatalog.RemoteJson.id)
                            add("unsupported-id")
                        },
                    )
                    "sourceOrder" -> put(
                        key,
                        buildJsonArray {
                            add("unsupported-id")
                            add(customSourceId)
                            add(customSourceId)
                        },
                    )
                    else -> put(key, value)
                }
            }
        }
        return json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), tampered)
    }
}
