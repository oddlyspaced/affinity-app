package com.oddlyspaced.surge.app.common.retrofit

import com.oddlyspaced.surge.app.common.modal.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @GET("/ping")
    suspend fun ping(): ResponseError

    @POST("/provider/add")
    suspend fun addProvider(@Body provider: Provider): ResponseError

    @POST("/provider/remove")
    suspend fun removeProvider(@Query("id") id: Int): ResponseError

    @GET("/provider/all")
    suspend fun fetchProviders(@Query("status") status: ProviderStatus): List<Provider>

    @GET("/provider/specific")
    suspend fun getProvider(
        @Query("id") id: Int
    ): Provider

    @POST("/provider/authenticate")
    suspend fun authenticateProvider(
        @Query("id") id: Int,
        @Query("password") password: String,
    ): ResponseError

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
    suspend fun updateProviderLocation(@Body location: LocationUpdateParameter): ResponseError

    @POST("/provider/update/area")
    suspend fun updateProviderServedArea(@Body area: AreaUpdateParameter): ResponseError

    @POST("/provider/update/status")
    suspend fun updateProviderStatus(@Body status: StatusUpdateParameter): ResponseError
}