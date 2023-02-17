package com.oddlyspaced.surge.manager.retrofit

import com.oddlyspaced.surge.app.common.nominatim.NominatimApi
import com.oddlyspaced.surge.app.common.nominatim.NominatimApiClient
import com.oddlyspaced.surge.app.common.retrofit.Api
import com.oddlyspaced.surge.app.common.retrofit.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object ApiModule {
    @Provides
    fun provideApi(): Api = ApiClient().getApiClient()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object NominatimApiModule {
    @Provides
    fun provideApi(): NominatimApi = NominatimApiClient().getApiClient()
}