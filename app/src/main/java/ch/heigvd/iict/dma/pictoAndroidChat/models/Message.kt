package ch.heigvd.iict.dma.pictoAndroidChat.models

import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import android.util.Base64

enum class MessageType {
    TEXT, DRAWING
}

data class Message(
    private var id: Int,
    private var sender: String,
    private var content: String,
    private var timestamp: Int,
    @SerializedName("type")
    private var messageType: MessageType = MessageType.TEXT,
    @SerializedName("drawing_data")
    private var drawingData: String? = null // Base64 encoded bitmap or drawing path data
) {

    // No secondary constructors needed with factory methods

    // Getters
    fun getId(): Int = id
    fun getSender(): String = sender
    fun getContent(): String = content
    fun getTimestamp(): Int = timestamp
    fun getMessageType(): MessageType = messageType
    fun getDrawingData(): String? = drawingData

    fun toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray()
    }

    override fun toString(): String {
        return when (messageType) {
            MessageType.TEXT -> this.content
            MessageType.DRAWING -> "[Drawing]"
        }
    }

    companion object {
        private var globalLocalId: Int = 0

        fun getGlobalId(): Int {
            return globalLocalId++
        }

        // Factory method for text messages
        fun createTextMessage(sender: String, content: String): Message {
            return Message(
                getGlobalId(),
                sender,
                content,
                System.currentTimeMillis().toInt(),
                MessageType.TEXT
            )
        }

        // Factory method for drawing messages
        fun createDrawingMessage(sender: String, drawingData: ByteArray): Message {
            val drawing = Base64.encodeToString(drawingData, Base64.DEFAULT)
            return Message(
                getGlobalId(),
                sender,
                "",
                System.currentTimeMillis().toInt(),
                MessageType.DRAWING,
                drawing
            )
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