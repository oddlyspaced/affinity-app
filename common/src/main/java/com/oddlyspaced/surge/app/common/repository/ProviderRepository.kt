package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

class ProviderRepository @Inject constructor(private val api: Api) {
    suspend fun addProvider(name: String, phone: PhoneNumber, location: Location, services: ArrayList<String>, areaServed: AreaServed) = api.addProvider(
        Provider(
            name = name,
            phone = phone,
            location = location,
            services = services,
            areaServed = areaServed,
            status = ProviderStatus.ACTIVE // not required
        )
    )
    suspend fun providers() = api.fetchProviders(status = ProviderStatus.ACTIVE)
    suspend fun providersUnfiltered() = api.fetchProviders(status = ProviderStatus.UNDEFINED)
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