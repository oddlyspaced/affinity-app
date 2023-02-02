package com.oddlyspaced.surge.app.common.retrofit

import com.oddlyspaced.surge.app.common.modal.Provider
import retrofit2.http.GET


interface Api {
    @GET("/provider/all")
    suspend fun fetchAllProviders(): ArrayList<Provider>
}