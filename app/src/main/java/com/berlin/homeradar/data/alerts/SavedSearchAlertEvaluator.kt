package com.berlin.homeradar.data.alerts

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.filter.matchesFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedSearchAlertEvaluator @Inject constructor(
    private val housingListingDao: HousingListingDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationManager: AppNotificationManager,
) {

    suspend fun evaluateAndNotify() {
        val listings = housingListingDao.getAllActive().map { it.toDomain() }
        val savedSearches = userPreferencesRepository.appSettingsSnapshot().savedSearches
        val seenBySearch = userPreferencesRepository.getSeenAlertMatches().toMutableMap()
        var changed = false

        savedSearches.filter { it.alertsEnabled }.forEach { savedSearch ->
            val matches = listings.filter { it.matchesFilter(savedSearch.filters) }
            val previous = seenBySearch[savedSearch.id].orEmpty()
            val newMatches = matches.filter { it.id !in previous }
            if (newMatches.isNotEmpty()) {
                notificationManager.showSavedSearchAlert(
                    searchName = savedSearch.name,
                    matchCount = newMatches.size,
                    listing = newMatches.first(),
                )
                seenBySearch[savedSearch.id] = (previous + newMatches.map { it.id }).toSet()
                changed = true
            }
        }

        if (changed) {
            userPreferencesRepository.markSeenAlertMatches(seenBySearch)
        }
    }
}
