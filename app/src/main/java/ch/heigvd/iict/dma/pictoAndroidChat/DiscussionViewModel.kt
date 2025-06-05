package ch.heigvd.iict.dma.pictoAndroidChat

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.DiscussionModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.LocalUserInfo
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import java.io.ByteArrayOutputStream
import java.util.Random

/**
 * ViewModel pour tout ce qui est en rapport avec les salles de discussions.
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 */

class DiscussionViewModel(val nearbyService : NearbyService) : ViewModel() {

    // Etat de la connexion
    enum class ConnectionState {
        DISCONNECTED, SCANNING, HOSTING, CONNECTED
    }

    // Instance du modèle
    private val discussionModel = DiscussionModel()

    // les LiveDatas privée
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    private val _localUserInfo = MutableLiveData<LocalUserInfo>(LocalUserInfo(randomUsername()))
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.DISCONNECTED)
    private val _errorState =
        MutableLiveData<String?>(null)

    // Les LiveDatas qui sont exposées
    val messages: LiveData<List<Message>> = _messages
    val localUserInfo: LiveData<LocalUserInfo> = _localUserInfo
    val connectionState: LiveData<ConnectionState> = _connectionState
    val errorState: LiveData<String?> = _errorState

    /**
     * Permet de synchroniser les message du LiveData et le modèle.
     */
    private fun refreshMessages() {
        _messages.value = discussionModel.getAllMessages()
    }

    /**
     * Permet de synchroniser les donnée de l'utilisateur du modèle et de la LiveData.
     */
    private fun refreshUserInfo() {
        _localUserInfo.value = discussionModel.localUserInfo
    }


    /**
     * Setter pour le nom d'utilisateur, en donne un aléatoire si le nom d'utilisateur donné est vide.
     * @param name le nouveau nom d'utilisateur que l'on veut donner.
     */
    fun setUsername(name: String) {
        val checkedName: String = if (name.isBlank()) randomUsername() else name
        discussionModel.localUserInfo = LocalUserInfo(checkedName)
        refreshUserInfo()
    }

    /**
     * Permet d'hébérger une nouvelle salle de discussion.
     * @param channelName permet de donner un nom à notre salle (pas utilisé car l'implémentation ne permet q'une salle).
     * @param maxUsers le nombre maximal d'utilisateur (pas utilisé).
     */
    fun hostChannel(channelName: String, maxUsers: Int) {
        try {
            nearbyService.startAdvertising()
            _connectionState.postValue(ConnectionState.CONNECTED)
        } catch (e: Exception) {
            _errorState.value = "Failed to host channel: ${e.message}"
        }
    }

    /**
     * Permet de remettre à zéro les valeurs utiles du ViewModel.
     */
    fun reset() {
        setUsername("")
        discussionModel.clearMessages()
        refreshMessages()
    }

    /**
     * Permet de rejoindre une salle de discussion.
     */
    fun scanForChannels() {
        _connectionState.postValue(ConnectionState.SCANNING)
        nearbyService.setOnConnectionEstablishedListener {
            _connectionState.postValue(ConnectionState.CONNECTED)
        }
        nearbyService.startDiscovery()
    }

    /**
     * Envoie un message qui contient du texte.
     * @param content Le corps du message.
     */
    fun sendTextMsg(content: String) {
        val msg = Message.createTextMessage(localUserInfo.value!!.name, content)
        sendMesage(msg)
        Log.d("DiscussionViewModel","Sending message: ${msg.getContent()}")
    }

    /**
     * Envoie un message qui contient un dessin.
     * @param drawingData Le dessin sous forme de Bitmap.
     */
    fun sendDrawingMsg(drawingData: Bitmap) {
        // Convertit la Bitmap en ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        drawingData.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val msg = Message.createDrawingMessage(localUserInfo.value!!.name, byteArray)
        sendMesage(msg)
        Log.d("DiscussionViewModel","Sending message: ${msg.getDrawingData()}")
    }

    /**
     * Envoie le message créé.
     * @param message le message à envoyer.
     */
    private fun sendMesage(message: Message) {
        discussionModel.addMessage(message)
        refreshMessages()
        nearbyService.sendPayload(message.toByteArray())
    }

    /**
     * La fonction appelée lorsqu'on recoit un message.
     * @param byteMessage Le message recu sous forme de ByteArray.
     */
    fun receiveMessage(byteMessage: ByteArray) {
        val message = Message.fromByteArray(byteMessage) ?: return
        discussionModel.addMessage(message)
        refreshMessages()
    }

    /**
     * Permet de se déconnecter de la salle de discussion.
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        nearbyService.disconnect()
    }


    override fun onCleared() {
        super.onCleared()
        // Nous déconnecte de la salle de discussion
        disconnect()
    }

    /**
     * Efface l'état d'erreur actuel.
     */
    fun clearError() {
        _errorState.value = null
    }


    companion object {
        private var instance: DiscussionViewModel? = null

        /**
         * Retourne l'instance singleton de DiscussionViewModel. Crée une nouvelle instance si elle n'existe pas encore.
         *
         * @param nearby Le service NearbyService à utiliser pour initialiser le ViewModel. Peut être null si l'instance existe déjà.
         * @return L'instance de DiscussionViewModel
         * @throws Exception Si nearby est null et qu'aucune instance n'existe
         */
        fun get(nearby: NearbyService? = null): DiscussionViewModel {
            if (instance == null) {
                if (nearby != null)
                    instance = DiscussionViewModel(nearby)
                else
                    throw Exception("Nearby is null")
            }

            return instance!!
        }

        /**
         * Permet d'obtenir un nom d'utilisateur aléatoire.
         * @return Un nom d'utilisateur de type String sous forme "User<RandomInt>".
         */
        fun randomUsername(): String {
            return "User${Random().nextInt(10000)}"
        }
    }
}
