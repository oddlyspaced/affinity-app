package com.oddlyspaced.surge.affinity.viewmodel

import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.affinity.repository.LocationRepository
import com.oddlyspaced.surge.affinity.repository.ProviderRepository
import com.oddlyspaced.surge.app_common.modal.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository): ViewModel() {
    val providers = repo.providers()
    var pickupLocation: Location? = null
    var pickupLocationAddress = ""
    var dropLocation: Location? = null
    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)
}