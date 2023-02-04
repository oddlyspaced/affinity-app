package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.SearchParameter
import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

class ProviderRepository @Inject constructor(private val api: Api) {
    suspend fun providers() = api.fetchAllProviders()
    suspend fun services() = api.fetchServiceTags()
    suspend fun search(params: SearchParameter) = api.searchProviders(
        params.limitCount,
        params.limitDistance,
        params.pickupLat,
        params.pickupLon,
        params.dropLat,
        params.dropLon,
    )
    suspend fun provider(id: Int) = api.getProvider(id)
    suspend fun saveProviderSource(id: Int, sourcePoint: Location, radius: Double) = api.updateProviderArea(id, sourcePoint.lat, sourcePoint.lon, radius)
    suspend fun updateProviderStatus(id: Int, status: Boolean) = api.updateProviderStatus(id, status)
}