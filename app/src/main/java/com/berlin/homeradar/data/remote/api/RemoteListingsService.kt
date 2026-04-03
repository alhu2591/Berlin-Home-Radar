package com.berlin.homeradar.data.remote.api

import retrofit2.http.GET

interface RemoteListingsService {
    @GET("listings.json")
    suspend fun getListings(): List<RemoteListingDto>
}
