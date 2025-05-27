package ch.heigvd.iict.dma.pictoAndroidChat.services

import android.R
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog.Builder
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import java.util.Random


class NearbyService(var context: Context) {

    val serviceId = "dma_pictochat"
    val STRATEGY = Strategy.P2P_CLUSTER
    var username: String? = null
        get()  {
            if(username == null)
                username = "User${Random().nextInt(10000)}"
            return username!!
        }
    var endpointId: String? = null


    public fun startAdvertising(){
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(context).startAdvertising(username!!, serviceId, connectionLifecycleCallback, options)
            .addOnSuccessListener {
                Log.d("NearbyService", "Advertising successfully started")
                //Nearby.getConnectionsClient(context).stopAdvertising()
            }
            .addOnFailureListener {
                Log.d("NearbyService", "Advertising failed ${it.message}")
            }
    }

    public fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(serviceId, endpointDiscoveryCallback, options)
            .addOnSuccessListener{
                Log.d("NearbyService", "Discovery successfully started")
            }
            .addOnFailureListener {
                Log.d("NearbyService", "Discovery failed ${it.message}")
            }
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // An endpoint was found. We request a connection to it.
            Nearby.getConnectionsClient(context)
                .requestConnection(username!!, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Log.d("NearbyService", "Connection successfully requested")
                }
                .addOnFailureListener {
                    Log.d("NearbyService", "Connection failed")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
            Log.d("NearbyService", "Endpoint lost")
        }
    }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Builder(context)
                .setTitle("Accept connection to " + info.endpointName)
                .setMessage("Confirm the code matches on both devices: " + info.authenticationDigits)
                .setPositiveButton(
                    "Accept"
                ) { dialog: DialogInterface?, which: Int ->  // The user confirmed, so we can accept the connection.
                    Nearby.getConnectionsClient(context)
                        .acceptConnection(endpointId, payloadCallback)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog: DialogInterface?, which: Int ->  // The user canceled, so we should reject the connection.
                    Nearby.getConnectionsClient(context).rejectConnection(endpointId)
                }
                .setIcon(R.drawable.ic_dialog_alert)
                .show()
        }

        override fun onConnectionResult(_endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("NearbyService", "Connection successfully established")
                    endpointId = _endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbyService", "Connection rejected")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("NearbyService", "Connection error")
                }
                else -> {}
            }
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.d("NearbyService", "Disconnected")
        }
    }

    private val payloadCallback : PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // This always gets the full data of the payload. Is null if it's not a BYTES payload.
            if (payload.getType() == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes()
                Log.d("NearbyService", "Received bytes: " + String(receivedBytes!!))
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
            // after the call to onPayloadReceived().
        }
    }

    public fun sendPayload(payload: ByteArray){
        if(endpointId == null){
            Log.d("NearbyService", "Endpoint id is null")
            return
        }
        val bytesPayload = Payload.fromBytes(payload)
        Nearby.getConnectionsClient(context).sendPayload(endpointId!!, bytesPayload)
    }
}