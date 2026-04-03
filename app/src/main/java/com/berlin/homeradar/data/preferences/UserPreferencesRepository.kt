package com.berlin.homeradar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("user_prefs.preferences_pb") }
    )

    private object Keys {
        val BackgroundSyncEnabled = booleanPreferencesKey("background_sync_enabled")
        val RemoteSourceEnabled = booleanPreferencesKey("remote_source_enabled")
    }

    val backgroundSyncEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.BackgroundSyncEnabled] ?: true
    }

    val remoteSourceEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.RemoteSourceEnabled] ?: false
    }

    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BackgroundSyncEnabled] = enabled }
    }

    suspend fun setRemoteSourceEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.RemoteSourceEnabled] = enabled }
    }
}
