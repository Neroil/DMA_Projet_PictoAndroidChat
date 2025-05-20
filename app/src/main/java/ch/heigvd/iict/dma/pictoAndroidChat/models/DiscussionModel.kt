package ch.heigvd.iict.dma.pictoAndroidChat.models

class DiscussionModel {
    private val messages = mutableListOf<Message>()
    var localUserInfo: LocalUserInfo? = null
    var currentChannel: ChannelInfo? = null

    fun addMessage(message: Message) {
        messages.add(message)
    }

    fun getAllMessages(): List<Message> {
        return messages.toList()
    }

    fun setChannel(channel: ChannelInfo) {
        currentChannel = channel
    }
}