package com.oddlyspaced.surge.affinity.retrofit

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