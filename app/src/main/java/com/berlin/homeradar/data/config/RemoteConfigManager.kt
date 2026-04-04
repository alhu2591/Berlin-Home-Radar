package com.berlin.homeradar.data.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.berlin.homeradar.BuildConfig
import com.berlin.homeradar.domain.model.RemoteConfigInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class RemoteConfigManager @Inject constructor(
    @ApplicationContext context: Context,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(REMOTE_CONFIG_FILE) },
    )

    private object Keys {
        val CachedJson = stringPreferencesKey("cached_json")
        val LastSuccessfulFetch = longPreferencesKey("last_successful_fetch")
        val LastError = stringPreferencesKey("last_error")
    }

    private val _info = MutableStateFlow(
        RemoteConfigInfo(
            endpointUrl = BuildConfig.REMOTE_CONFIG_URL.takeIf { it.isNotBlank() },
            isEnabled = BuildConfig.REMOTE_CONFIG_URL.isNotBlank(),
        ),
    )
    val info = _info.asStateFlow()

    suspend fun loadCachedConfig() {
        val prefs = dataStore.data.first()
        val cachedJson = prefs[Keys.CachedJson]
        val lastSuccessfulFetch = prefs[Keys.LastSuccessfulFetch]
        val lastError = prefs[Keys.LastError]
        if (!cachedJson.isNullOrBlank()) {
            apply(json.decodeFromString(RemoteRuntimeConfigPayload.serializer(), cachedJson))
        }
        _info.value = _info.value.copy(
            lastSuccessfulFetchMillis = lastSuccessfulFetch,
            lastErrorMessage = lastError,
        )
    }

    suspend fun refreshIfStale(maxAgeMillis: Long = 6 * 60 * 60 * 1000) {
        val url = BuildConfig.REMOTE_CONFIG_URL.takeIf { it.isNotBlank() } ?: return
        val now = System.currentTimeMillis()
        val lastFetch = _info.value.lastSuccessfulFetchMillis
        if (lastFetch != null && now - lastFetch < maxAgeMillis) return
        refresh(url)
    }

    suspend fun refresh(url: String = BuildConfig.REMOTE_CONFIG_URL): Result<Unit> {
        if (url.isBlank()) return Result.failure(IllegalStateException("Remote config URL is blank"))
        return runCatching {
            val request = Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Remote config request failed with ${response.code}" }
                val raw = response.body?.string().orEmpty()
                val payload = json.decodeFromString(RemoteRuntimeConfigPayload.serializer(), raw)
                apply(payload)
                dataStore.edit { prefs ->
                    prefs[Keys.CachedJson] = raw
                    prefs[Keys.LastSuccessfulFetch] = System.currentTimeMillis()
                    prefs[Keys.LastError] = ""
                }
                _info.value = _info.value.copy(
                    lastSuccessfulFetchMillis = System.currentTimeMillis(),
                    lastErrorMessage = null,
                )
            }
        }.onFailure { throwable ->
            dataStore.edit { prefs ->
                prefs[Keys.LastError] = throwable.message.orEmpty()
            }
            _info.value = _info.value.copy(lastErrorMessage = throwable.message)
        }
    }

    private fun apply(payload: RemoteRuntimeConfigPayload) {
        FeatureFlags.updateFromRemote(payload)
    }

    companion object {
        private const val REMOTE_CONFIG_FILE = "remote_config.preferences_pb"
    }
}
