package com.oddlyspaced.surge.manager.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oddlyspaced.surge.app.common.databinding.ActivityCheckBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * entry point for app. ensures the server is reachable and handles location permission status
 */
@AndroidEntryPoint
class CheckActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCheckBinding.inflate(layoutInflater) }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val vm: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // check if server is active
                checkServerActive()
            }
            else {
                // show dialog saying app cant be used
                showPermissionNotGrantedPrompt()
            }
        }
        handleLocationPermission()
    }

    private fun handleLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            MaterialAlertDialogBuilder(this).apply {
                setTitle("Location Permission Required")
                setMessage("In order to manage and set location based information, this app requires the location permission to function.")
                setPositiveButton("Grant") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                setNegativeButton("Decline") { _, _ ->
                    finishAffinity()
                }
                setCancelable(false)
            }.show()
        }
        else {
            checkServerActive()
        }
    }

    private fun showPermissionNotGrantedPrompt() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Location Permission Required")
            setMessage("This app cannot function without location permission, please grant it from the app settings page in order to continue.")
            setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
            setCancelable(false)
        }.show()
    }

    private fun checkServerActive() {
        vm.ping().observe(this) {
            if (!it.error) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
            else {
                showServerUnavailablePrompt()
            }
        }
    }

    private fun showServerUnavailablePrompt() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Unable to reach server")
            setMessage("Unable to establish connection to server, please check your internet connection.")
            setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            setCancelable(false)
        }.show()
    }
}