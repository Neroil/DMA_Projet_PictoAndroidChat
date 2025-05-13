package ch.heigvd.iict.dma.pictoAndroidChat.models

data class Message(
    private var id: Int,
    private var sender: String,
    private var content: String,
    private var timestamp: Int
) {
    companion object {
    }
}