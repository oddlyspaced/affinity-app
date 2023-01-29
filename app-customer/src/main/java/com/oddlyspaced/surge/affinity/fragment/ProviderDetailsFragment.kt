package com.oddlyspaced.surge.affinity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.oddlyspaced.surge.affinity.databinding.LayoutBottomSheetProviderDetailsBinding

class ProviderDetailsFragment: BottomSheetDialogFragment() {

    companion object {
        fun create(): ProviderDetailsFragment {
            return ProviderDetailsFragment()
        }
    }

    private lateinit var binding: LayoutBottomSheetProviderDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutBottomSheetProviderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
}