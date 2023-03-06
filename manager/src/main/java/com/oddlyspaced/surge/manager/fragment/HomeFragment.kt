package com.oddlyspaced.surge.manager.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.adapter.ProviderListAdapter
import com.oddlyspaced.surge.manager.adapter.swipe.RecyclerTouchListener
import com.oddlyspaced.surge.manager.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

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
                adapter = ProviderListAdapter(providers, findNavController())
            }
            val touchListener = RecyclerTouchListener(requireActivity(), binding.rvHomeProviderList)
            touchListener.setClickable(object : RecyclerTouchListener.OnRowClickListener {
                override fun onRowClicked(position: Int) {
                }

                override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
                }
            })
                .setSwipeOptionViews(R.id.delete_task, R.id.edit_task)
                .setSwipeable(
                    R.id.layout_item_provider_root, R.id.rowBG
                ) { viewId, position ->
                    when (viewId) {
                        R.id.delete_task -> {
                            Toast.makeText(requireContext(), "Not Available", Toast.LENGTH_SHORT).show()
                        }
                        R.id.edit_task -> {
                            Toast.makeText(requireContext(), "Edit Not Available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            binding.rvHomeProviderList.addOnItemTouchListener(touchListener)
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