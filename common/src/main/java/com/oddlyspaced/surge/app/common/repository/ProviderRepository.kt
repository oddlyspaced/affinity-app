package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

class ProviderRepository @Inject constructor(private val api: Api) {
    suspend fun addProvider(id: Int, name: String, phone: PhoneNumber, location: Location, services: ArrayList<String>, areaServed: AreaServed) = api.addProvider(
        Provider(
            id = id,
            name = name,
            phone = phone,
            location = location,
            services = services,
            areaServed = areaServed,
            status = ProviderStatus.ACTIVE // not required
        )
    )
    suspend fun deleteProvider(id: Int) = api.removeProvider(id)
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
    suspend fun updateProviderStatus(id: Int, status: ProviderStatus) = api.updateProviderStatus(StatusUpdateParameter(id, status))
    suspend fun updateProviderLocation(id: Int, location: Location) = api.updateProviderLocation(LocationUpdateParameter(id, location))
    suspend fun updateProviderServedArea(id: Int, areaServed: AreaServed) = api.updateProviderServedArea(AreaUpdateParameter(id, areaServed))
    suspend fun authenticateProvider(id: Int, password: String) = api.authenticateProvider(id, password)
}