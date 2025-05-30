package ch.heigvd.iict.dma.pictoAndroidChat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.ChannelInfo
import ch.heigvd.iict.dma.pictoAndroidChat.models.DiscussionModel
import ch.heigvd.iict.dma.pictoAndroidChat.models.LocalUserInfo
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService

class DiscussionViewModel : ViewModel() {

    // Connection states
    enum class ConnectionState {
        DISCONNECTED, SCANNING, HOSTING, CONNECTED
    }

    // Model instance
    private val discussionModel = DiscussionModel()
    //private val nearbyService = NearbyService()

    // Private LiveData
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    private val _localUserInfo = MutableLiveData<LocalUserInfo?>()
    private val _currentChannel = MutableLiveData<ChannelInfo?>()
    private val _availableChannels = MutableLiveData<List<ChannelInfo>>(emptyList())
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.DISCONNECTED)
    private val _currentDrawing = MutableLiveData<ByteArray?>(null)
    private val _errorState = MutableLiveData<String?>(null) // Used to communicate errors to the UI (Toasts)

    // Public LiveData exposed to the UI
    val messages: LiveData<List<Message>> = _messages
    val localUserInfo: LiveData<LocalUserInfo?> = _localUserInfo
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

    private fun refreshChannel() {
        _currentChannel.value = discussionModel.currentChannel
    }

    fun setUsername(name: String) {
        discussionModel.localUserInfo = LocalUserInfo(name)
        refreshUserInfo()
    }

    fun hostChannel(channelName: String, maxUsers: Int) {
        try {
            val channel = ChannelInfo(channelName, 1, maxUsers)
            discussionModel.setChannel(channel)
            refreshChannel()
            _connectionState.value = ConnectionState.HOSTING
            //nearbyService.host()
            // TODO: Broadcast channel info
        } catch (e: Exception) {
            _errorState.value = "Failed to host channel: ${e.message}"
        }
    }

    fun scanForChannels() {
        _connectionState.value = ConnectionState.SCANNING
        // Implementation for scanning would update _availableChannels when found
        // TODO: Implement channel scanning using NearbyService

    }

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

    fun sendMessage(content: String) {
        val username = discussionModel.localUserInfo?.name ?: "Unknown"
        val channelId = discussionModel.currentChannel?.id

        if (channelId != null) {
            try {
                val message = Message(username, content, channelId)
                discussionModel.addMessage(message)
                refreshMessages()
                // TODO: Broadcast message to other users using NearbyService
            } catch (e: Exception) {
                _errorState.value = "Failed to send message: ${e.message}"
            }
        } else {
            _errorState.value = "Cannot send message: Not connected to a channel"
        }
    }

    fun addMessage(message: Message) {
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
}