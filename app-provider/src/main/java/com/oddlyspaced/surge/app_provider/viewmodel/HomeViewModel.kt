package com.oddlyspaced.surge.app_provider.viewmodel

import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app_common.modal.Location
import com.oddlyspaced.surge.app_common.repository.LocationRepository
import com.oddlyspaced.surge.app_common.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository): ViewModel() {
    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)
}