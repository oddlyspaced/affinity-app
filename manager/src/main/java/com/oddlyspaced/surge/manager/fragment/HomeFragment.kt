package com.oddlyspaced.surge.manager.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
//        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFragment())
    }
}