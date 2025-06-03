package ch.heigvd.iict.dma.pictoAndroidChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import kotlin.collections.toTypedArray
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message

class MainActivity : AppCompatActivity() {

    private lateinit var nearbyService: NearbyService
    private val permissionsGranted = MutableLiveData(false)
    private lateinit var discussionViewModel: DiscussionViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = mutableListOf(Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        // we request permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
            permissions += Manifest.permission.BLUETOOTH_SCAN
            permissions += Manifest.permission.BLUETOOTH_CONNECT
            permissions += Manifest.permission.BLUETOOTH_ADVERTISE

        }
        else {

            permissions += Manifest.permission.BLUETOOTH
            permissions += Manifest.permission.BLUETOOTH_ADMIN

        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2)
            permissions += Manifest.permission.NEARBY_WIFI_DEVICES

        requestNearbyPermissionLauncher.launch(permissions.toTypedArray())


    }
    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService(this)
        discussionViewModel = DiscussionViewModel(nearbyService)

        findViewById<Button>(R.id.host_button).setOnClickListener {
            discussionViewModel.hostChannel("channel1", 10)
        }

        findViewById<Button>(R.id.join_button).setOnClickListener {
            discussionViewModel.scanForChannels()
        }

        findViewById<Button>(R.id.send).setOnClickListener {
            discussionViewModel.sendMessage("Hello zebi")
        }

        val messageObserver = Observer<List<Message>> { messages->
            // Update the UI, in this case, a TextView.
            if(!messages.isEmpty() )
                Toast.makeText(this, "Msg: ${ messages.last()}, Size: ${messages.size}", Toast.LENGTH_SHORT).show()
        }

        discussionViewModel.messages.observe(this, messageObserver)
    }

    private val requestNearbyPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val grantedBase = permissions.getOrDefault(Manifest.permission.ACCESS_WIFI_STATE, false) &&
                permissions.getOrDefault(Manifest.permission.CHANGE_WIFI_STATE, false) &&
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)



        val granted31 = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADVERTISE, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false) &&
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) &&
                    permissions.getOrDefault(Manifest.permission.ACCESS_WIFI_STATE, false)
        else
            permissions.getOrDefault(Manifest.permission.BLUETOOTH, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADMIN, false)

        val granted32 = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2)
            permissions.getOrDefault(Manifest.permission.NEARBY_WIFI_DEVICES, false)
        else
            true

        if (grantedBase && granted31 && granted32) {
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

