package ch.heigvd.iict.dma.pictoAndroidChat.services

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog.Builder
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

/**
 * Service de gestion des connexions Nearby pour la communication peer-to-peer.
 * Permet de créer et rejoindre des salles de discussion via Google Nearby Connections API.
 *
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 */
class NearbyService(var context: Context) {

    val serviceId = "dma_pictochat"
    val STRATEGY = Strategy.P2P_CLUSTER

    var endpointIds: Set<String> = emptySet()
    var isHost: Boolean = false
    val username = ""

    var onConnectionEstablished: (() -> Unit)? = null
    var onMessageReceived: ((ByteArray) -> Unit)? = null
    var onDisconnection: (() -> Unit)? = null

    /**
     * Définit le callback appelé lorsqu'une connexion est établie.
     * @param listener La fonction à appeler lors de l'établissement de la connexion
     */
    fun setOnConnectionEstablishedListener(listener: () -> Unit) {
        onConnectionEstablished = listener
    }

    /**
     * Définit le callback appelé lors d'une déconnexion.
     * @param listener La fonction à appeler lors de la déconnexion
     */
    fun setOnDisconnectionListener(listener: () -> Unit) {
        onDisconnection = listener
    }

    /**
     * Définit le callback appelé lors de la réception d'un message.
     * @param listener La fonction à appeler avec les données reçues
     */
    fun setOnMessageReceivedListener(listener: (ByteArray) -> Unit) {
        onMessageReceived = listener
    }

    /**
     * Démarre la publicité pour permettre à d'autres appareils de découvrir ce service.
     * L'appareil devient hôte de la salle de discussion.
     */
    public fun startAdvertising(){
        try {
            val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
            Nearby.getConnectionsClient(context).startAdvertising(username, serviceId, connectionLifecycleCallback, options)
                .addOnSuccessListener {
                    Log.d("NearbyService", "Advertising successfully started")
                    isHost = true
                }
                .addOnFailureListener {
                    Log.d("NearbyService", "Advertising failed ${it.message}")
                }
        } catch (e: Exception) {
            Log.d("NearbyService", "Advertising failed ${e.message}")
        }
    }

    /**
     * Démarre la découverte d'autres appareils qui publient ce service.
     * Permet de rejoindre une salle de discussion existante.
     */
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

    /**
     * Callback pour la découverte d'endpoints.
     * Gère la découverte et la perte d'appareils disponibles.
     */
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // Un endpoint a été trouvé. Nous demandons une connexion avec lui.
            Nearby.getConnectionsClient(context)
                .requestConnection(username, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Log.d("NearbyService", "Connection successfully requested")
                    Nearby.getConnectionsClient(context).stopDiscovery()
                }
                .addOnFailureListener {
                    Log.d("NearbyService", "Connection failed")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            // Un endpoint précédemment découvert a disparu.
            Log.d("NearbyService", "Endpoint lost")
        }
    }

    /**
     * Callback pour le cycle de vie des connexions.
     * Gère l'initiation, le résultat et la déconnexion des connexions.
     */
    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Builder(context)
                .setTitle("Accept connection to " + info.endpointName)
                .setMessage("Confirm the code matches on both devices: " + info.authenticationDigits)
                .setPositiveButton(
                    "Accept"
                ) { dialog: DialogInterface?, which: Int ->  // L'utilisateur a confirmé, nous pouvons accepter la connexion.
                    Nearby.getConnectionsClient(context)
                        .acceptConnection(endpointId, payloadCallback)
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog: DialogInterface?, which: Int ->  // L'utilisateur a annulé, nous devons rejeter la connexion.
                    Nearby.getConnectionsClient(context).rejectConnection(endpointId)
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("NearbyService", "Connection successfully established")
                    endpointIds +=  endpointId

                    if (onConnectionEstablished != null) {
                        onConnectionEstablished!!()
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbyService", "Connection rejected")
                    if(!isHost){
                        startDiscovery()
                    }
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("NearbyService", "Connection error")
                    if(!isHost){
                        startDiscovery()
                    }
                }
                else -> {}
            }
        }

        override fun onDisconnected(endpointId: String) {
            // Nous avons été déconnectés de cet endpoint. Aucune donnée ne peut plus
            // être envoyée ou reçue.
            Log.d("NearbyService", "Disconnected")
            endpointIds -= endpointId
            if(!isHost && onDisconnection != null){
                onDisconnection?.invoke()
            }
        }
    }

    /**
     * Callback pour la gestion des données reçues.
     * Traite les messages entrants et les redistribue si l'appareil est hôte.
     */
    private val payloadCallback : PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // Ceci récupère toujours toutes les données du payload. Est null si ce n'est pas un payload BYTES.
            if (payload.getType() == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes()
                Log.d("NearbyService", "Received bytes: " + String(receivedBytes!!))
                if (onMessageReceived != null) {
                    onMessageReceived!!(receivedBytes)
                }

                if (isHost){
                    // Envoie les bytes reçus aux autres appareils
                    endpointIds.forEach { otherEndpointId ->
                        if (otherEndpointId != endpointId) {
                            Nearby.getConnectionsClient(context).sendPayload(otherEndpointId, payload)
                        }
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Les payloads de bytes sont envoyés en un seul bloc, vous recevrez donc une mise à jour SUCCESS immédiatement
            // après l'appel à onPayloadReceived().
        }
    }

    /**
     * Envoie des données à tous les appareils connectés.
     * @param payload Les données à envoyer sous forme de ByteArray
     */
    public fun sendPayload(payload: ByteArray){
        if(endpointIds.isEmpty()){
            Log.d("NearbyService", "No devices connected")
            return
        }
        val bytesPayload = Payload.fromBytes(payload)
        endpointIds.forEach { endpointId ->
            Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload)
        }
    }

    /**
     * Déconnecte tous les endpoints et remet à zéro l'état du service.
     */
    fun disconnect(){
        Nearby.getConnectionsClient(context).stopAllEndpoints()
        endpointIds = emptySet()
        isHost = false
    }

    companion object{
        private var instance: NearbyService? = null

        /**
         * Retourne l'instance singleton de NearbyService.
         * Crée une nouvelle instance si elle n'existe pas encore.
         *
         * @param context Le contexte Android à utiliser pour initialiser le service.
         *               Peut être null si l'instance existe déjà.
         * @return L'instance de NearbyService
         * @throws Exception Si context est null et qu'aucune instance n'existe
         */
        fun get(context: Context? = null): NearbyService {
            if (instance == null) {
                if(context != null)
                    instance = NearbyService(context)
                else
                    throw Exception("Context is null")
            }
            else if(context != null)
                instance!!.context = context
            return instance!!
        }
    }
}