package com.oddlyspaced.surge.app_common.retrofit

import com.oddlyspaced.surge.app_common.modal.Provider
import retrofit2.http.GET


interface Api {
    @GET("/provider/all")
    suspend fun fetchAllProviders(): ArrayList<Provider>
}