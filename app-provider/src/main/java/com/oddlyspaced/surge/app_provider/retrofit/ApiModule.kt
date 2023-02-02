package com.oddlyspaced.surge.app_provider.retrofit

import com.oddlyspaced.surge.app_common.nominatim.NominatimApi
import com.oddlyspaced.surge.app_common.nominatim.NominatimApiClient
import com.oddlyspaced.surge.app_common.retrofit.Api
import com.oddlyspaced.surge.app_common.retrofit.ApiClient
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