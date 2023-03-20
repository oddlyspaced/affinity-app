package com.oddlyspaced.surge.manager.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.flip
import com.oddlyspaced.surge.app.common.toast
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.adapter.ProviderListAdapter
import com.oddlyspaced.surge.manager.adapter.swipe.RecyclerTouchListener
import com.oddlyspaced.surge.manager.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * main screen for the app
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val vm: ManagerViewModel by activityViewModels()
    private lateinit var adapter: ProviderListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        init()
    }

    private fun fetchProviders() {
        vm.fetchProviders(vm.providers.isEmpty()).observe(requireActivity()) { providers ->
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
            adapter = ProviderListAdapter(arrayListOf<Provider>().apply { addAll(providers) }, findNavController())
            binding.rvHomeProviderList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@HomeFragment.adapter
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
                            // delete provider
                            val provider = adapter.items[position]
                            vm.deleteProvider(provider.id).observe(requireActivity()) {
                                if (it.error) {
                                    toast("An error occurred while trying to remove provider!")
                                }
                                else {
                                    adapter.items.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                }
                            }
                        }
                        R.id.edit_task -> {
                            // update provider for status
                            val provider = adapter.items[position]
                            vm.updateProviderStatus(provider.id, provider.status.flip()).observe(requireActivity()) {
                                if (it.error) {
                                    toast("An error occurred while trying to update status!")
                                }
                                else {
                                    provider.status = provider.status.flip()
                                    adapter.items[position] = provider
                                    adapter.notifyItemChanged(position)
                                }
                            }
                        }
                    }
                }
            binding.rvHomeProviderList.addOnItemTouchListener(touchListener)
        }
    }

    private fun init() {
        fetchProviders()
        binding.btnHomeFetch.setOnClickListener {
            fetchProviders()
        }
        binding.fabHomeEdit.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFragment())
        }
    }

}