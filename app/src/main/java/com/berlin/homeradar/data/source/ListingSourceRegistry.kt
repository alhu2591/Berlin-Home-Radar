package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.config.FeatureFlags
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.SourceDefinition
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient

@Singleton
class ListingSourceRegistry @Inject constructor(
    private val bundledJsonListingSource: BundledJsonListingSource,
    private val bundledHtmlListingSource: BundledHtmlListingSource,
    private val remoteJsonListingSource: RemoteJsonListingSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val okHttpClient: OkHttpClient,
) {
    private val builtInSources by lazy {
        listOf(
            bundledJsonListingSource,
            bundledHtmlListingSource,
            remoteJsonListingSource,
        ).associateBy { it.sourceId }
    }

    suspend fun getEnabledSources(): List<ListingSource> {
        val settings = userPreferencesRepository.appSettings.first()
        val allDefinitions = (SourceCatalog.all + settings.customSources).associateBy { it.id }
        return settings.sourceOrder
            .mapNotNull { sourceId ->
                val definition = allDefinitions[sourceId]
                adapterFor(definition)
            }
            .filter { source -> source.sourceId in settings.enabledSourceIds }
            .filter { source -> FeatureFlags.isSourceEnabled(source.sourceId) }
    }

    fun adapterFor(definition: SourceDefinition?): ListingSource? {
        definition ?: return null
        if (!FeatureFlags.isSourceEnabled(definition.id)) return null
        return builtInSources[definition.id]
            ?: if (definition.isUserAdded || definition.supportsAutomatedSync) {
                CustomConfiguredListingSource(definition, okHttpClient)
            } else {
                null
            }
    }

    fun findSource(sourceId: String): ListingSource? {
        val builtIn = builtInSources[sourceId]
        return builtIn
    }
}
