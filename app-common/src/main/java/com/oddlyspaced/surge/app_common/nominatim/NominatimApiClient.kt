package com.oddlyspaced.surge.app_common.nominatim

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NominatimApiClient {
    private lateinit var client: NominatimApi
    fun getApiClient(): NominatimApi {
        if (!this::client.isInitialized) {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
            client = Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(NominatimApi::class.java)
        }
        return client
    }
}