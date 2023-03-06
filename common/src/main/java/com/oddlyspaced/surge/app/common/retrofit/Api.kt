package com.oddlyspaced.surge.app.common.retrofit

import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.ProviderStatus
import com.oddlyspaced.surge.app.common.modal.ResponseError
import com.oddlyspaced.surge.app.common.modal.Service
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @GET("/ping")
    suspend fun ping(): ResponseError

    @POST("/provider/add")
    suspend fun addProvider(@Body provider: Provider): ResponseError

    @GET("/provider/all")
    suspend fun fetchProviders(@Query("status") status: ProviderStatus): List<Provider>

    @GET("/provider/specific")
    suspend fun getProvider(
        @Query("id") id: Int
    ): Provider

    @GET("/provider/services")
    suspend fun fetchServiceTags(): ArrayList<Service>

    @POST("/provider/search")
    suspend fun searchProviders(
        @Query("limitCount") limitCount: Int,
        @Query("limitDistance") limitDistance: Int,
        @Query("pickupLat") pickupLat: Double,
        @Query("pickupLon") pickupLon: Double,
        @Query("dropLat") dropLat: Double,
        @Query("dropLon") dropLon: Double,
    ): ArrayList<Provider>

    @POST("/provider/update/location")
    suspend fun updateProviderLocation(

    )

    suspend fun updateProviderArea(
        @Query("id") id: Int,
        @Query("sourceLat") lat: Double,
        @Query("sourceLon") lon: Double,
        @Query("radius") radius: Double,
    )

    @POST("/provider/statusupdate")
    suspend fun updateProviderStatus(
        @Query("id") id: Int,
        @Query("isActive") status: Boolean,
    )
}