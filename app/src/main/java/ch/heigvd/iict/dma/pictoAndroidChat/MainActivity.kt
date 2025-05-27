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

class MainActivity : AppCompatActivity() {

    private lateinit var nearbyService: NearbyService
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // we request permissions
        requestPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                var allPermissionsGranted = true
                permissions.entries.forEach {
                    if (!it.value) {
                        allPermissionsGranted = false
                        // Log or inform the user that a specific permission was denied
                        Log.w("Permissions", "Permission denied: ${it.key}")
                    }
                }

                if (allPermissionsGranted) {
                    // All required permissions are granted, proceed with Nearby operations
                    Log.d("Permissions", "All required permissions were granted.")
                } else {
                    // Not all permissions were granted.
                    // You might want to show a dialog explaining why the permissions are needed
                    // or disable the functionality that depends on these permissions.
                    Log.e("Permissions", "Not all required permissions were granted.")
                    // Example: show a snackbar or dialog
                    // Snackbar.make(findViewById(android.R.id.content), "Required permissions were not granted.", Snackbar.LENGTH_LONG).show()
                }
            }

        checkAndRequestNearbyPermissions()
    }
    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService(this)
        nearbyService.startDiscovery()
    }

    private fun checkAndRequestNearbyPermissions() {
        val requiredPermissions = getRequiredPermissions()

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest)
        } else {
            // All permissions are already granted
            //startNearbyOperations()
            Log.d("Permissions", "All required permissions were granted.")
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // Wi-Fi State Permissions (up to Android 12 - API 31)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.ACCESS_WIFI_STATE)
            permissions.add(Manifest.permission.CHANGE_WIFI_STATE)
        }

        // Classic Bluetooth Permissions (up to Android 11 - API 30)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // Location Permissions
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 (API 28) and below
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) { // Android 10 (API 29) to Android 12 (API 31)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // For Android 12 (API 31) and above, BLUETOOTH_SCAN (if used for location) requires ACCESS_FINE_LOCATION.
        // The Nearby Connections library often handles this, but it's good to be aware.
        // New Bluetooth Permissions (Android 12 - API 31 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Nearby Wi-Fi Devices (Android 13 - API 32 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        return permissions.distinct() // Ensure no duplicates if logic overlaps
    }
    }

