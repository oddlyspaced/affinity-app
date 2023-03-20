package com.oddlyspaced.surge.manager.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.modal.*
import com.oddlyspaced.surge.app.common.toast
import com.oddlyspaced.surge.manager.BuildConfig
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.FragmentAddEditBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon

@AndroidEntryPoint
class AddEditFragment: Fragment(R.layout.fragment_add_edit) {

    private lateinit var userLocationMarker: Marker

    private val polygonGeoPoints = arrayListOf<GeoPoint>()
    private lateinit var areaPolygon: Polygon

    private lateinit var binding: FragmentAddEditBinding
    private val vm: ManagerViewModel by activityViewModels()
    private val args: AddEditFragmentArgs by navArgs()

    private var isAddingProvider = false
    private val navController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAddEditBinding.bind(view)
        initOSMDroid()

        if (args.providerId == -1) { // no provider id passed
            if (vm.sourcePointAddress != null) {
                markSelectedSourceLocation()
            }
        }
        else {
            loadProviderDetails(args.providerId)
        }
        init()
    }

    private fun loadProviderDetails(id: Int) {
        vm.fetchProvider(id).observe(requireActivity()) {
            setProviderDetails(it)
        }
    }

    private fun setProviderDetails(provider: Provider) {
        binding.etAddEditName.setText(provider.name)
        binding.etAddEditCode.setText(provider.phone.countryCode)
        binding.etAddEditPhone.setText(provider.phone.phoneNumber)
        binding.etAddEditServices.setText(provider.services.joinToString(","))
        vm.sourcePointAddress = Address(provider.location, "")
        markSelectedSourceLocation()
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

    private fun init() {
        binding.viewTouchMap.setOnClickListener {
            findNavController().navigate(AddEditFragmentDirections.actionAddFragmentToEditFragment())
        }

        binding.btnAddEditSave.setOnClickListener {
            handleSave()
        }
    }

    private fun handleSave() {
        binding.etAddEditName.text.let { name ->
            if (name.isEmpty()) {
                toast("Please enter a name")
                return
            }
        }
        binding.etAddEditCode.text.let { code ->
            if (code.isEmpty()) {
                toast("Please enter a phone number code")
                return
            }
        }
        binding.etAddEditPhone.text.let { phone ->
            if (phone.isEmpty()) {
                toast("Please enter a phone number")
                return
            }
        }
        binding.etAddEditServices.text.let { services ->
            if (services.isEmpty()) {
                toast("Please enter services list")
                return
            }
//            if (services.split(",").toTypedArray())
        }
        vm.sourcePointAddress?.let {} ?: run {
            toast("Please select area to be served")
            return
        }

        if (isAddingProvider) {
            toast("Please wait for previous call to finish")
            return
        }

        isAddingProvider = true
        binding.layoutAddSaving.isVisible = true
        vm.addProvider(
            id = args.providerId,
            name = binding.etAddEditName.text.toString(),
            phone = PhoneNumber(
                countryCode = binding.etAddEditCode.text.toString(),
                phoneNumber = binding.etAddEditPhone.text.toString()
            ),
            location = Location(90.0, 135.0),
            services = arrayListOf<String>().apply {
                addAll(
                    binding.etAddEditServices.text.toString().split(",")
                )
            },
            areaServed = AreaServed(vm.sourcePointAddress!!.location, vm.sourcePointWorkingRadius)
        ).observe(requireActivity()) { response ->
            isAddingProvider = false
            binding.layoutAddSaving.isVisible = false
            if (response.error) {
                toast("An error occured while trying to save provider!")
            }
            else {
                vm.providers.filter { provider ->
                    provider.id == args.providerId
                }.let { filteredProviders ->
                    if (filteredProviders.isEmpty()) {
                        // new provider
                        vm.providers.clear()
                    } else {
                        // old provider, just update details
                        toast("Provider updated successfully")
                        vm.providers[vm.providers.indexOf(filteredProviders[0])] = Provider(
                            id = args.providerId,
                            name = binding.etAddEditName.text.toString(),
                            phone = PhoneNumber(
                                countryCode = binding.etAddEditCode.text.toString(),
                                phoneNumber = binding.etAddEditPhone.text.toString()
                            ),
                            location = Location(90.0, 135.0),
                            services = arrayListOf<String>().apply {
                                addAll(
                                    binding.etAddEditServices.text.toString().split(",")
                                )
                            },
                            areaServed = AreaServed(
                                vm.sourcePointAddress!!.location,
                                vm.sourcePointWorkingRadius
                            ),
                            status = filteredProviders[0].status
                        )
                    }
                }
                navController.popBackStack()
            }
        }
    }

    private fun markSelectedSourceLocation() {
        vm.sourcePointAddress?.location?.let { location ->
            var items: ItemizedIconOverlay<OverlayItem>? = null
            val markers = arrayListOf<OverlayItem>()
            val item = OverlayItem("", "", GeoPoint(location.lat, location.lon))
            item.setMarker(ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location))
            markers.add(item)
            items = ItemizedIconOverlay(requireActivity(), markers, null)
            toast("Marking selected location")
            requireActivity().runOnUiThread {
                binding.map.controller.setZoom(AffinityConfiguration.DEFAULT_MAP_ZOOM)
                binding.map.controller.setCenter(GeoPoint(location.lat, location.lon))
                binding.map.overlays.add(items)
                binding.map.invalidate()
                createCircleAroundPoint(GeoPoint(location.lat, location.lon), vm.sourcePointWorkingRadius)
            }
        }
    }

    private fun createCircleAroundPoint(point: GeoPoint, radius: Double) {
        Logger.d("Drawing circle for $point")
        polygonGeoPoints.clear()
        for (i in 0 until 360) {
            polygonGeoPoints.add(point.destinationPoint(radius * 1000, i.toDouble()))
        }
        handlePolygon()
    }

    private fun handlePolygon() {
        if (this::areaPolygon.isInitialized) {
            binding.map.overlayManager.remove(areaPolygon)
        }
        areaPolygon = Polygon().apply {
            title = "Sample Title"
            fillPaint.color = Color.BLUE
            fillPaint.alpha = 128
            outlinePaint.color = Color.BLACK
            isVisible = true
            points = polygonGeoPoints
        }
        binding.map.overlayManager.add(areaPolygon)
        binding.map.invalidate()
    }

}