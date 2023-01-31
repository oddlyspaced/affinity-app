package com.oddlyspaced.surge.affinity.retrofit

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import retrofit2.http.GET
import retrofit2.http.POST
import com.oddlyspaced.surge.app_common.modal.*

interface Api {
    @GET("/provider/all")
    suspend fun fetchAllProviders(): ArrayList<Provider>
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object ApiModule {
    @Provides
    fun provideApi(): Api = ApiClient().getApiClient()
}
