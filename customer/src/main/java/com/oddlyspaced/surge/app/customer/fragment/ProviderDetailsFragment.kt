package com.oddlyspaced.surge.app.customer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.oddlyspaced.surge.app.customer.adapter.ProviderListAdapter
import com.oddlyspaced.surge.app.customer.databinding.LayoutBottomSheetProviderDetailsBinding

class ProviderDetailsFragment: BottomSheetDialogFragment() {

    companion object {
        fun create(): ProviderDetailsFragment {
            return ProviderDetailsFragment()
        }
    }

    private lateinit var binding: LayoutBottomSheetProviderDetailsBinding
    private val args: ProviderDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutBottomSheetProviderDetailsBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        val provider = args.providers
        binding.rvProviderList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ProviderListAdapter(provider.providers, args.baseLocation)
        }
//        binding.apply {
//            txProviderInfoName.text = provider.name
//            txProviderInfoDistance.text = "13 km away" // todo: calculate real distance
//       }
//        provider.services.forEach { service ->
//            binding.chipGroupProviderInfoService.addView(Chip(requireContext()).apply {
//                text = service
//            })
//        }
    }
}