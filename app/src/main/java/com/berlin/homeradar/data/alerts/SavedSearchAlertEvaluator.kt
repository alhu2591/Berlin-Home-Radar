package com.berlin.homeradar.data.alerts

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedSearchAlertEvaluator @Inject constructor(
    private val housingListingDao: HousingListingDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationManager: AppNotificationManager,
) {

    suspend fun evaluateAndNotify() {
        val listings = housingListingDao.getAll().map { it.toDomain() }
        val savedSearches = userPreferencesRepository.appSettingsSnapshot().savedSearches
        val seenBySearch = userPreferencesRepository.getSeenAlertMatches().toMutableMap()
        var changed = false

        savedSearches.filter { it.alertsEnabled }.forEach { savedSearch ->
            val matches = listings.filter { it.matches(savedSearch.filters) }
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

private fun HousingListing.matches(filter: ListingFilterPreset): Boolean {
    val matchesQuery = filter.query.isBlank() || buildString {
        append(title); append(' '); append(location); append(' '); append(district); append(' '); append(source)
    }.contains(filter.query, ignoreCase = true)
    val matchesRooms = filter.minRooms == null || rooms >= filter.minRooms
    val matchesArea = filter.minArea == null || areaSqm >= filter.minArea
    val matchesPrice = filter.maxPrice == null || priceEuro <= filter.maxPrice
    val matchesDistrict = filter.district.isNullOrBlank() || district.equals(filter.district, ignoreCase = true)
    val matchesSource = filter.selectedSourceIds.isEmpty() || source in filter.selectedSourceIds
    val matchesJobcenter = !filter.onlyJobcenter || isJobcenterSuitable
    val matchesWohngeld = !filter.onlyWohngeld || isWohngeldEligible
    val matchesWbs = !filter.onlyWbs || isWbsRequired
    val matchesFavorites = !filter.showFavoritesOnly || isFavorite
    return matchesQuery && matchesRooms && matchesArea && matchesPrice &&
        matchesDistrict && matchesSource && matchesJobcenter &&
        matchesWohngeld && matchesWbs && matchesFavorites
}
