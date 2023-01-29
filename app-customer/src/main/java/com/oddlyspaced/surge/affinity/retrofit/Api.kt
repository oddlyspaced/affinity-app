package com.oddlyspaced.surge.affinity.retrofit

import com.oddlyspaced.surge.affinity.modal.Provider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import retrofit2.http.GET
import retrofit2.http.POST

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
