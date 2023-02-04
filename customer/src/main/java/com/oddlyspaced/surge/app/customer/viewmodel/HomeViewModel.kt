package com.oddlyspaced.surge.app.customer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.repository.LocationRepository
import com.oddlyspaced.surge.app.common.repository.ProviderRepository
import com.oddlyspaced.surge.app.common.modal.Address
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository): ViewModel() {

    private var providersFetchedAt = 0L
    private val _providers = MutableLiveData<ArrayList<Provider>>()
    val providers: LiveData<ArrayList<Provider>>
    get() {
        if (System.currentTimeMillis() - providersFetchedAt > 5 * 1000L) {
            // need to refresh
            Logger.d("Fetching Providers!")
            CoroutineScope(Dispatchers.IO).launch {
                _providers.postValue(repo.providers())
                providersFetchedAt = System.currentTimeMillis()
            }
        }
       return _providers
    }

    val selectedLocation = hashMapOf<LocationType, Address>()
    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)

    val services: LiveData<ArrayList<Service>>
    get() {
        val _services = MutableLiveData<ArrayList<Service>>()
        CoroutineScope(Dispatchers.IO).launch {
            _services.postValue(repo.services())
        }
        return _services
    }
}

enum class LocationType {
    PICKUP, DROP
}