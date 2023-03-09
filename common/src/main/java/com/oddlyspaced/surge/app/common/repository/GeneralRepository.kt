package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

/**
 * repository class to hold method calls of general non categorized endpoints
 */
class GeneralRepository @Inject constructor(private val api: Api) {
    // ping call to check server availability
    suspend fun ping() = api.ping()
}