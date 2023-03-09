package com.oddlyspaced.surge.app.common.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.nominatim.NominatimApi
import com.oddlyspaced.surge.app.common.nominatim.modal.ReverseSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * repository class to hold method calls to endpoint related to nominatim api
 */
class LocationRepository @Inject constructor(private val api: NominatimApi) {
    fun address(location: Location, zoom: Int): LiveData<ReverseSearchResult> {
        val data = MutableLiveData<ReverseSearchResult>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(api.reverseSearch(location.lat, location.lon, zoom))
        }
        return data
    }

}