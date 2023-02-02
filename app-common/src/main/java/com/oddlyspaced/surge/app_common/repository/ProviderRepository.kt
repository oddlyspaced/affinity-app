package com.oddlyspaced.surge.app_common.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oddlyspaced.surge.app_common.modal.Provider
import com.oddlyspaced.surge.app_common.retrofit.Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProviderRepository @Inject constructor(private val api: Api) {

    fun providers(): LiveData<ArrayList<Provider>> {
        val data = MutableLiveData<ArrayList<Provider>>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(api.fetchAllProviders())
        }
        return data
    }

}