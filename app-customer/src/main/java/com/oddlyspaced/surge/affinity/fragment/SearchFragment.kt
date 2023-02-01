package com.oddlyspaced.surge.affinity.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentSearchBinding

class SearchFragment: Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSearchBinding.bind(view)
    }

}