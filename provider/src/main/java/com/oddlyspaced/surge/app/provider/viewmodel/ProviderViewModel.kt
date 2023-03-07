package com.oddlyspaced.surge.app.provider.viewmodel

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
class ProviderViewModel @Inject constructor(private val repo: ProviderRepository, private val locationRepository: LocationRepository, private val generalRepository: GeneralRepository): ViewModel() {

    var providerId: Int = -1

    var sourcePointAddress: Address? = null
    var sourcePointWorkingRadius: Double = -1.0

    fun addressFromLocation(location: Location, zoom: Int) = locationRepository.address(location, zoom)

    fun fetchProvider(id: Int): LiveData<Provider> {
        val data = MutableLiveData<Provider>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.provider(id))
        }
        return data
    }

    fun updateProviderStatus(id: Int, status: ProviderStatus): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.updateProviderStatus(id, status))
        }
        return data
    }

    fun updateProviderLocation(id: Int, location: Location): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.updateProviderLocation(id, location))
        }
        return data
    }

    fun updateProviderAreaServed(id: Int, areaServed: AreaServed): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(repo.updateProviderServedArea(id, areaServed))
        }
        return data
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

    fun authenticate(id: Int, password: String): LiveData<ResponseError> {
        val data = MutableLiveData<ResponseError>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                data.postValue(repo.authenticateProvider(id, password))
            }
            catch (e: Exception) {
                e.printStackTrace()
                data.postValue(ResponseError("Unable to login", error = true))
            }
        }
        return data
    }
}