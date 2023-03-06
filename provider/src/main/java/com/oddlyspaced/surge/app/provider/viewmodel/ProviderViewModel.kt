package com.oddlyspaced.surge.app.provider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.modal.Address
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.ResponseError
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
            // todo
//            repo.updateProviderStatus(1, status)
        }
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