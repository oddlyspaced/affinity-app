package com.oddlyspaced.surge.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.repository.LocationRepository
import com.oddlyspaced.surge.app.common.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository) : ViewModel() {

    var sourcePointAddress: Address? = null
    var sourcePointWorkingRadius: Double = -1.0

    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)


    fun addProvider(name: String, phone: PhoneNumber, location: Location, services: ArrayList<String>, areaServed: AreaServed): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.addProvider(name, phone, location, services, areaServed))
        }
        return data
    }




    private val _providers = MutableLiveData<List<Provider>>()
    val providers: LiveData<List<Provider>>
        get() {
            // need to refresh
            Logger.d("Fetching Providers!")
            CoroutineScope(Dispatchers.IO).launch {
                _providers.postValue(repo.providersUnfiltered())
            }
            return _providers
        }

}