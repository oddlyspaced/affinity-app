package com.oddlyspaced.surge.affinity.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oddlyspaced.surge.app_common.modal.Location
import com.oddlyspaced.surge.app_common.nominatim.NominatimApi
import com.oddlyspaced.surge.app_common.nominatim.modal.ReverseSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationRepository @Inject constructor(private val api: NominatimApi) {

    fun address(location: Location, zoom: Int): LiveData<ReverseSearchResult> {
        val data = MutableLiveData<ReverseSearchResult>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(api.reverseSearch(location.lat, location.lon, zoom))
        }
        return data
    }

}