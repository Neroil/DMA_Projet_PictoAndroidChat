package ch.heigvd.iict.dma.pictoAndroidChat.models

import com.google.gson.Gson

data class Message(
    private var id: Int,
    private var sender: String,
    private var content: String,
    private var timestamp: Int,
    private var channelId: Int,
) {

    // Constructor to send a message to any channel
    constructor(sender: String, content: String, channelId: Int) : this(getGlobalId(), sender, content,
        System.currentTimeMillis().toInt(), channelId)

    fun toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray()
    }

    companion object {

        private var globalLocalId: Int = 0

        fun getGlobalId(): Int {
            return globalLocalId++
        }

        fun fromByteArray(bytes: ByteArray): Message? {
            return try {
                val json = String(bytes)
                Gson().fromJson(json, Message::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}