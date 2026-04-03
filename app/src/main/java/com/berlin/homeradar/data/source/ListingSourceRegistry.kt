package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingSourceRegistry @Inject constructor(
    private val bundledJsonListingSource: BundledJsonListingSource,
    private val bundledHtmlListingSource: BundledHtmlListingSource,
    private val remoteJsonListingSource: RemoteJsonListingSource,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun getEnabledSources(): List<ListingSource> {
        val remoteEnabled = userPreferencesRepository.remoteSourceEnabled.first()
        return buildList {
            add(bundledJsonListingSource)
            add(bundledHtmlListingSource)
            if (remoteEnabled) add(remoteJsonListingSource)
        }
    }
}
