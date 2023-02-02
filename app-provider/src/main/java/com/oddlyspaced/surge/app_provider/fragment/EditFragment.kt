package com.oddlyspaced.surge.app_provider.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.oddlyspaced.surge.app_provider.R
import com.oddlyspaced.surge.app_provider.databinding.FragmentEditBinding

class EditFragment: Fragment(R.layout.fragment_edit) {

    private lateinit var binding: FragmentEditBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEditBinding.bind(view)
    }
}