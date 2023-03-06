package com.oddlyspaced.surge.app.common.repository

import com.oddlyspaced.surge.app.common.retrofit.Api
import javax.inject.Inject

// repository class containing endpoint functions that are not related to any particular context
class GeneralRepository @Inject constructor(private val api: Api) {
    suspend fun ping() = api.ping()
}