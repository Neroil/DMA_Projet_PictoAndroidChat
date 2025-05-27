package ch.heigvd.iict.dma.pictoAndroidChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.toTypedArray
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

class MainActivity : AppCompatActivity() {

    private lateinit var nearbyService: NearbyService
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private val permissionsGranted = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // we request permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestNearbyPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE))
        }
        else {
            requestNearbyPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }


    }
    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService(this)
        permissionsGranted.observe(this) { granted ->
            if (granted)
                nearbyService.startDiscovery()
        }
    }

    private val requestNearbyPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val isBLEScanGranted =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false)
        else
            true
        val isFineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val isCoarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (isBLEScanGranted && (isFineLocationGranted || isCoarseLocationGranted) ) {
            // Permission is granted. Continue the action
            permissionsGranted.postValue(true)

        }
        else {
            // Explain to the user that the feature is unavailable
            Toast.makeText(this, "haaaaaaaaaaaaaa", Toast.LENGTH_SHORT).show()
            permissionsGranted.postValue(false)
        }
    }

}

