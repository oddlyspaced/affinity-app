package com.oddlyspaced.surge.app.common.nominatim

import com.oddlyspaced.surge.app.common.nominatim.modal.ReverseSearchResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * api interface for nominatim reverse search api
 */
interface NominatimApi {
    @GET("reverse.php")
    suspend fun reverseSearch(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("zoom") zoom: Int, @Query("format") format: String = "jsonv2"): ReverseSearchResult
}