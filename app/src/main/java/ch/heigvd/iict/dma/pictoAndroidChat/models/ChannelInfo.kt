package ch.heigvd.iict.dma.pictoAndroidChat.models

import com.google.gson.Gson

/**
 * ChannelInfo are the info needed when 
 */
data class ChannelInfo(
    val name: String,
    val currentUsers: Int,
    val maxUsers: Int,
    val id: Int
) {
    fun toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray()
    }

    constructor(name: String, currentUsers: Int, maxUsers: Int) : this(name, currentUsers, maxUsers, getGlobalId())

    companion object {

        private var globalLocalId: Int = 0
        fun getGlobalId(): Int {
            return globalLocalId++
        }
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