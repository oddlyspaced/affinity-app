package com.oddlyspaced.surge.manager.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.freelapp.libs.locationfetcher.locationFetcher
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.applyFrom
import com.oddlyspaced.surge.app.common.asGeoPoint
import com.oddlyspaced.surge.manager.BuildConfig
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.FragmentAddEditBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class AddEditFragment: Fragment(R.layout.fragment_add_edit) {

    private lateinit var userLocationMarker: Marker
    private var currentMarker: Marker? = null

    private lateinit var binding: FragmentAddEditBinding

    private val locationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAddEditBinding.bind(view)

        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            markCurrentLocation()
        }
    }

    // handles runtime map configuration
    private fun initOSMDroid() {
        // setup map configuration
        Configuration.getInstance().let { config ->
            config.load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
            config.userAgentValue = BuildConfig.APPLICATION_ID
        }

        // setting up map
        binding.map.apply {
            setUseDataConnection(true)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }

        // zooming in by default
        binding.map.controller.setZoom(AffinityConfiguration.DEFAULT_MAP_ZOOM)
    }

    private suspend fun markCurrentLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                if (this@AddEditFragment::userLocationMarker.isInitialized) {
                    requireActivity().runOnUiThread {
                        binding.map.overlays.remove(userLocationMarker)
                    }
                }
                userLocationMarker = Marker(binding.map).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(
                        Color.BLUE) }
                    setInfoWindow(null)
                }
                requireActivity().runOnUiThread {
                    binding.map.controller.setCenter(userPoint)
                    binding.map.overlays.add(userLocationMarker)
                }
            })
        }

    }
}