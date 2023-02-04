package com.oddlyspaced.surge.app.common.retrofit

import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.Service
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface Api {
    @GET("/provider/all")
    suspend fun fetchAllProviders(): ArrayList<Provider>
    @GET("/provider/services")
    suspend fun fetchServiceTags(): ArrayList<Service>
    @GET("/provider/search")
    suspend fun searchProviders(@Query("limitCount") limitCount: Int, @Query("limitDistance") limitDistance: Int, @Query("pickupPoint") pickupPoint: Location, @Query("dropPoint") dropPoint: Location, @Query("filterServices") filterServices: ArrayList<String>): ArrayList<Provider>
}