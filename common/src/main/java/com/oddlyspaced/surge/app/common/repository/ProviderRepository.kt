package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

class ProviderRepository @Inject constructor(private val api: Api) {
    suspend fun providers() = api.fetchAllProviders()
}