package com.oddlyspaced.surge.app.common.retrofit

import com.oddlyspaced.surge.app.common.modal.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * api interface for affinity central backend api
 */
interface Api {

    @GET("/ping")
    suspend fun ping(): ServerResponse

    @POST("/provider/add")
    suspend fun addProvider(@Body provider: Provider): ServerResponse

    @POST("/provider/remove")
    suspend fun removeProvider(@Query("id") id: Int): ServerResponse

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
    ): ServerResponse

    @GET("/provider/services")
    suspend fun fetchServiceTags(): ArrayList<Service>

    @POST("/provider/search")
    suspend fun searchProviders(@Body params: SearchParameter): List<Provider>

    @POST("/provider/update/location")
    suspend fun updateProviderLocation(@Body location: LocationUpdateParameter): ServerResponse

    @POST("/provider/update/area")
    suspend fun updateProviderServedArea(@Body area: AreaUpdateParameter): ServerResponse

    @POST("/provider/update/status")
    suspend fun updateProviderStatus(@Body status: StatusUpdateParameter): ServerResponse
}