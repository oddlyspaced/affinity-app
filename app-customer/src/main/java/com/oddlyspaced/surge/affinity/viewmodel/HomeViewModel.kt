package com.oddlyspaced.surge.affinity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oddlyspaced.surge.app_common.modal.Provider
import com.oddlyspaced.surge.affinity.repository.ProviderRepository
import com.oddlyspaced.surge.app_common.modal.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: ProviderRepository): ViewModel() {
    val providers = repo.providers()
    var pickupLocation: Location? = null
    var dropLocation: Location? = null
}