package com.oddlyspaced.surge.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(private val repo: ProviderRepository) : ViewModel() {

    private val _providers = MutableLiveData<ArrayList<Provider>>()
    val providers: LiveData<ArrayList<Provider>>
        get() {
            // need to refresh
            Logger.d("Fetching Providers!")
            CoroutineScope(Dispatchers.IO).launch {
                _providers.postValue(repo.providers())
            }
            return _providers
        }

}