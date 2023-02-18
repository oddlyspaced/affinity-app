package com.oddlyspaced.surge.manager.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.adapter.ProviderListAdapter
import com.oddlyspaced.surge.manager.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val vm: ManagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        init()
    }

    private fun fetchProviders() {
        vm.providers.observe(requireActivity()) { providers ->
            displayProviders(providers)
        }
    }

    private fun displayProviders(providers: List<Provider>) {
        if (providers.isEmpty()) {
            binding.rvHomeProviderList.isVisible = false
            binding.layoutHomeLoading.isVisible = true
            binding.txHomeLoading.text = "No Providers Listed"
            binding.pbHomeLoading.isVisible = false
        }
        else {
            binding.rvHomeProviderList.isVisible = true
            binding.layoutHomeLoading.isVisible = false
            binding.rvHomeProviderList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ProviderListAdapter(providers)
            }
        }
    }

    private fun init() {
        binding.btnHomeFetch.setOnClickListener {
            fetchProviders()
        }
        binding.fabHomeEdit.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFragment())
        }
    }

}