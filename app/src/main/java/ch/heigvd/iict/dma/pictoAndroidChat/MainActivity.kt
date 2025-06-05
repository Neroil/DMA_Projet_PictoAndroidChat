package ch.heigvd.iict.dma.pictoAndroidChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import kotlin.collections.toTypedArray
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.heigvd.iict.dma.pictoAndroidChat.DiscussionViewModel.ConnectionState

/**
 * Activité principale de l'application de chat.
 * Gère les permissions, l'initialisation des services et la navigation vers les discussions.
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 */

class MainActivity : AppCompatActivity() {

    private lateinit var nearbyService: NearbyService
    private val permissionsGranted = MutableLiveData(false)
    private lateinit var discussionViewModel: DiscussionViewModel

    /**
     * Initialise l'activité et demande les permissions nécessaires.
     * Configure les permissions selon la version Android.
     *
     * @param savedInstanceState L'état sauvegardé de l'activité
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = mutableListOf(Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        // On demande les permissions
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

    /**
     * Configure les services, le ViewModel et les observateurs au démarrage de l'activité.
     * Initialise les listeners pour les boutons et les callbacks pour les messages.
     */
    override fun onStart() {
        super.onStart()

        // Crée les services et viewmodels
        nearbyService = NearbyService.get(this)
        discussionViewModel = DiscussionViewModel.get(nearbyService)
        discussionViewModel.reset()
        
        
        // Assigne les listeners aux boutons

        findViewById<Button>(R.id.host_button).setOnClickListener {
            discussionViewModel.hostChannel("channel1", 10)
        }

        findViewById<Button>(R.id.join_button).setOnClickListener {
            Toast.makeText(this, "Attempting to join a room...", Toast.LENGTH_SHORT).show()
            discussionViewModel.scanForChannels()
        }

        // Défini le callback pour les messages reçus
        nearbyService.setOnMessageReceivedListener {
            discussionViewModel.receiveMessage(it)
        }

        nearbyService.setOnDisconnectionListener{
            discussionViewModel.disconnect()
        }

        // Observe les changements de connexion
        val connectionObserver = Observer<ConnectionState> { state ->
            Log.d("MainActivity", "Connection state changed to $state")
            when (state) {
                ConnectionState.CONNECTED -> {
                    val username = findViewById<EditText>(R.id.username_input).text
                    if (!username.isEmpty()){
                        discussionViewModel.setUsername(username.toString())
                    }
                    val i = Intent(this , DiscussionActivity::class.java)
                    startActivity(i)
                }

                else -> {}
            }
        }

        discussionViewModel.connectionState.observe(this, connectionObserver)
        Log.d("MainActivity", "onStart")
    }

    /**
     * Gestionnaire de résultat pour les demandes de permissions multiples.
     * Vérifie si toutes les permissions nécessaires ont été accordées selon la version Android.
     */
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
            // Les permissions nous ont été données
            permissionsGranted.postValue(true)

        }
        else {
            // Toast to indicate a permission wasn't granted
            Toast.makeText(this, "Permission is missing", Toast.LENGTH_SHORT).show()
            permissionsGranted.postValue(false)
        }
    }

}

