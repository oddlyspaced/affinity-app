package com.oddlyspaced.surge.app.customer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.repository.GeneralRepository
import com.oddlyspaced.surge.app.common.repository.LocationRepository
import com.oddlyspaced.surge.app.common.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository, private val generalRepository: GeneralRepository): ViewModel() {

    private var providersFetchedAt = 0L
    private val _providers = MutableLiveData<List<Provider>>()
    val providers: LiveData<List<Provider>>
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

    fun fetchServices(): LiveData<ArrayList<Service>> {
        val data = MutableLiveData<ArrayList<Service>>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.services())
        }
        return data
    }

    fun search(params: SearchParameter): LiveData<ArrayList<Provider>> {
        val _search = MutableLiveData<ArrayList<Provider>>()
        CoroutineScope(Dispatchers.IO).launch {
            _search.postValue(repo.search(params))
        }
        return _search
    }

    fun ping(): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                data.postValue(generalRepository.ping())
            }
            catch (e: Exception) {
                data.postValue(ResponseError("Unable to reach server", true))
                Logger.d("Error in ping")
                e.printStackTrace()            }
        }
        return data
    }
}

enum class LocationType {
    PICKUP, DROP
}