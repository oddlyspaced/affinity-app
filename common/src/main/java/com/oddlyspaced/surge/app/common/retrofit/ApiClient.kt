package com.oddlyspaced.surge.app.common.retrofit

import com.google.gson.GsonBuilder
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    private lateinit var client: Api
    fun getApiClient(): Api {
        if (!this::client.isInitialized) {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
            client = Retrofit.Builder()
                .baseUrl(AffinityConfiguration.API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(Api::class.java)
        }
        return client
    }
}