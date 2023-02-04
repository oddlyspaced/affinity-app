package com.oddlyspaced.surge.app.provider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.modal.Address
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.repository.LocationRepository
import com.oddlyspaced.surge.app.common.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository): ViewModel() {
    var sourcePointAddress: Address? = null
    var sourcePointWorkingRadius: Double = -1.0
    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)
    fun getProviderInfo(id: Int): LiveData<Provider> {
        val _provider = MutableLiveData<Provider>()
        CoroutineScope(Dispatchers.IO).launch {
            _provider.postValue(repo.provider(id))
        }
        return _provider
    }
    fun updateProviderSourceArea(id: Int, sourcePoint: Location, radius: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.saveProviderSource(1, sourcePoint, radius)
        }
    }
    fun updateProviderStatus(id: Int, status: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateProviderStatus(1, status)
        }
    }
}