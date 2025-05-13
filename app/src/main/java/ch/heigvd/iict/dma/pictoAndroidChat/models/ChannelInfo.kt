package ch.heigvd.iict.dma.pictoAndroidChat.models

import com.google.gson.Gson

/**
 * ChannelInfo are the info needed when 
 */
data class ChannelInfo(
    val name: String,
    val currentUsers: Int,
    val maxUsers: Int,
    val hostId: String
) {
    fun toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray()
    }

    companion object {
        fun fromByteArray(bytes: ByteArray): ChannelInfo? {
            return try {
                val json = String(bytes)
                Gson().fromJson(json, ChannelInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}