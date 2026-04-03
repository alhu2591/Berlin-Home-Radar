package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.source.model.RawListing

interface ListingSource {
    val sourceId: String

    suspend fun fetch(): List<RawListing>

    suspend fun healthCheck(): Result<Unit> = runCatching {
        fetch()
        Unit
    }
}
