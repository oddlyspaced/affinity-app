package com.oddlyspaced.surge.affinity.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oddlyspaced.surge.affinity.modal.Provider
import com.oddlyspaced.surge.affinity.retrofit.Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KSuspendFunction0

class ProviderRepository @Inject constructor(private val api: Api) {

    fun providers(): LiveData<ArrayList<Provider>> {
        val data = MutableLiveData<ArrayList<Provider>>()
        CoroutineScope(Dispatchers.IO).launch {
            data.postValue(api.fetchAllProviders())
        }
        return data
    }

}