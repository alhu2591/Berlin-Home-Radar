package com.berlin.homeradar.domain.filter

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset

fun HousingListing.matchesFilter(filter: ListingFilterPreset): Boolean {
    val matchesQuery = filter.query.isBlank() || searchableText()
        .contains(filter.query, ignoreCase = true)
    val matchesRooms = filter.minRooms == null || rooms >= filter.minRooms
    val matchesArea = filter.minArea == null || areaSqm >= filter.minArea
    val matchesPrice = filter.maxPrice == null || priceEuro <= filter.maxPrice
    val matchesDistrict = filter.district.isNullOrBlank() || district.equals(filter.district, ignoreCase = true)
    val matchesSource = filter.selectedSourceIds.isEmpty() || source in filter.selectedSourceIds
    val matchesJobcenter = !filter.onlyJobcenter || isJobcenterSuitable
    val matchesWohngeld = !filter.onlyWohngeld || isWohngeldEligible
    val matchesWbs = !filter.onlyWbs || isWbsRequired
    val matchesFavorites = !filter.showFavoritesOnly || isFavorite
    return matchesQuery &&
        matchesRooms &&
        matchesArea &&
        matchesPrice &&
        matchesDistrict &&
        matchesSource &&
        matchesJobcenter &&
        matchesWohngeld &&
        matchesWbs &&
        matchesFavorites
}

private fun HousingListing.searchableText(): String = buildString {
    append(title)
    append(' ')
    append(location)
    append(' ')
    append(district)
    append(' ')
    append(source)
}
