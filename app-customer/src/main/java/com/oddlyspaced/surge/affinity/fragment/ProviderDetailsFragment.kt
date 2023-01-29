package com.oddlyspaced.surge.affinity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.oddlyspaced.surge.affinity.databinding.LayoutBottomSheetProviderDetailsBinding

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
        val provider = args.provider
        binding.apply {
            txProviderInfoName.text = provider.name
            txProviderInfoDistance.text = "13 km away" // todo: calculate real distance
        }
    }
}