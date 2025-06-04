package ch.heigvd.iict.dma.pictoAndroidChat

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.ChannelInfo
import ch.heigvd.iict.dma.pictoAndroidChat.models.DiscussionModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.LocalUserInfo
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import java.io.ByteArrayOutputStream
import java.util.Random


class DiscussionViewModel(val nearbyService : NearbyService) : ViewModel() {

    // Connection states
    enum class ConnectionState {
        DISCONNECTED, SCANNING, HOSTING, CONNECTED
    }

    // Model instance
    private val discussionModel = DiscussionModel()

    // Private LiveData
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    private val _localUserInfo = MutableLiveData<LocalUserInfo>(LocalUserInfo(randomUsername()))
    private val _currentChannel = MutableLiveData<ChannelInfo?>()
    private val _availableChannels = MutableLiveData<List<ChannelInfo>>(emptyList())
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.DISCONNECTED)
    private val _currentDrawing = MutableLiveData<ByteArray?>(null)
    private val _errorState =
        MutableLiveData<String?>(null) // Used to communicate errors to the UI (Toasts)

    // Public LiveData exposed to the UI
    val messages: LiveData<List<Message>> = _messages
    val localUserInfo: LiveData<LocalUserInfo> = _localUserInfo
    val currentChannel: LiveData<ChannelInfo?> = _currentChannel
    val availableChannels: LiveData<List<ChannelInfo>> = _availableChannels
    val connectionState: LiveData<ConnectionState> = _connectionState
    val currentDrawing: LiveData<ByteArray?> = _currentDrawing
    val errorState: LiveData<String?> = _errorState

    // Sync model with LiveData
    private fun refreshMessages() {
        _messages.value = discussionModel.getAllMessages()
    }

    private fun refreshUserInfo() {
        _localUserInfo.value = discussionModel.localUserInfo
    }

    /*
    private fun refreshChannel() {
        _currentChannel.value = discussionModel.currentChannel
    }
    */

    fun setUsername(name: String) {
        val checkedName: String = if (name.isBlank()) randomUsername() else name
        discussionModel.localUserInfo = LocalUserInfo(checkedName)
        refreshUserInfo()
    }

    fun hostChannel(channelName: String, maxUsers: Int) {
        try {
            nearbyService.startAdvertising()
            _connectionState.postValue(ConnectionState.CONNECTED)
        } catch (e: Exception) {
            _errorState.value = "Failed to host channel: ${e.message}"
        }
    }

    fun scanForChannels() {
        _connectionState.postValue(ConnectionState.SCANNING)
        // Implementation for scanning would update _availableChannels when found
        nearbyService.setOnConnectionEstablishedListener {
            _connectionState.postValue(ConnectionState.CONNECTED)
        }
        nearbyService.startDiscovery()
    }

    /*
    fun joinChannel(channel: ChannelInfo) {
        try {
            discussionModel.setChannel(channel)
            refreshChannel()
            _connectionState.value = ConnectionState.CONNECTED
            // TODO: Implement channel joining using NearbyService
        } catch (e: Exception) {
            _errorState.value = "Failed to join channel: ${e.message}"
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    */

    fun sendTextMsg(content: String) {
        val msg = Message.createTextMessage(localUserInfo.value!!.name, content)
        sendMesage(msg)
        Log.d("DiscussionViewModel","Sending message: ${msg.getContent()}")
    }

    fun sendDrawingMsg(drawingData: Bitmap) {
        // Convert bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        drawingData.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val msg = Message.createDrawingMessage(localUserInfo.value!!.name, byteArray)
        sendMesage(msg)
        Log.d("DiscussionViewModel","Sending message: ${msg.getDrawingData()}")
    }

    fun sendMesage(message: Message) {
        discussionModel.addMessage(message)
        refreshMessages()
        nearbyService.sendPayload(message.toByteArray())
    }

    fun receiveMessage(byteMessage: ByteArray) {
        val message = Message.fromByteArray(byteMessage) ?: return
        discussionModel.addMessage(message)
        refreshMessages()
    }


    fun updateDrawing(drawingData: ByteArray) {
        _currentDrawing.value = drawingData
        // TODO: Broadcast drawing to other users
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        // TODO: Disconnect from NearbyService
    }

    fun clearError() {
        _errorState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    companion object {
        private var instance: DiscussionViewModel? = null
        fun get(nearby: NearbyService? = null): DiscussionViewModel {
            if (instance == null) {
                if (nearby != null)
                    instance = DiscussionViewModel(nearby)
                else
                    throw Exception("Neaby is null")
            }

            return instance!!
        }

        fun randomUsername(): String {
            return "User${Random().nextInt(10000)}"
        }
    }
}
