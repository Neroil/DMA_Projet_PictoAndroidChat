package ch.heigvd.iict.dma.pictoAndroidChat.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DiscussionModel {
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _currentChannel = MutableLiveData<ChannelInfo?>()
    val currentChannel: LiveData<ChannelInfo?> = _currentChannel

    fun addMessage(message: Message) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }

    fun setChannel(channel: ChannelInfo) {
        _currentChannel.value = channel
    }
}