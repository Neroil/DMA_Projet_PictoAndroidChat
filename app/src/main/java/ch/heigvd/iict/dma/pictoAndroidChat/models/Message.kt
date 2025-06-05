package ch.heigvd.iict.dma.pictoAndroidChat.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import android.util.Base64

/**
 * Énumération des types de messages supportés.
 */
enum class MessageType {
    /** Message contenant du texte */
    TEXT,
    /** Message contenant un dessin */
    DRAWING
}

/**
 * Classe représentant un message dans l'application de chat.
 * Peut contenir du texte ou des données de dessin encodées en Base64.
 *
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 *
 * @param id Identifiant unique du message
 * @param sender Nom de l'expéditeur du message
 * @param content Contenu textuel du message
 * @param timestamp Horodatage de création du message
 * @param messageType Type du message (TEXT ou DRAWING)
 * @param drawingData Données du dessin encodées en Base64 (optionnel)
 */
data class Message(
    private var id: Int,
    private var sender: String,
    private var content: String,
    private var timestamp: Int,
    @SerializedName("type")
    private var messageType: MessageType = MessageType.TEXT,
    @SerializedName("drawing_data")
    private var drawingData: String? = null // Bitmap encodé en Base64 ou données de chemin de dessin
) {

    /**
     * Retourne l'identifiant du message.
     * @return L'ID du message
     */
    fun getId(): Int = id

    /**
     * Retourne l'expéditeur du message.
     * @return Le nom de l'expéditeur
     */
    fun getSender(): String = sender

    /**
     * Retourne le contenu textuel du message.
     * @return Le contenu du message
     */
    fun getContent(): String = content

    /**
     * Retourne l'horodatage du message.
     * @return Le timestamp de création
     */
    fun getTimestamp(): Int = timestamp

    /**
     * Retourne le type du message.
     * @return Le type de message (TEXT ou DRAWING)
     */
    fun getMessageType(): MessageType = messageType

    /**
     * Retourne les données de dessin encodées en Base64.
     * @return Les données de dessin ou null si le message n'est pas un dessin
     */
    fun getDrawingData(): String? = drawingData

    /**
     * Convertit le message en tableau de bytes pour la transmission.
     * @return Le message sérialisé en JSON sous forme de ByteArray
     */
    fun toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray()
    }

    /**
     * Retourne une représentation textuelle du message.
     * @return Le contenu pour les messages texte, "[Drawing]" pour les dessins
     */
    override fun toString(): String {
        return when (messageType) {
            MessageType.TEXT -> this.content
            MessageType.DRAWING -> "[Drawing]"
        }
    }

    companion object {
        private var globalLocalId: Int = 0

        /**
         * Génère et retourne un identifiant unique global.
         * @return Un nouvel ID unique
         */
        fun getGlobalId(): Int {
            return globalLocalId++
        }

        /**
         * Méthode factory pour créer un message texte.
         * @param sender Le nom de l'expéditeur
         * @param content Le contenu textuel du message
         * @return Un nouveau message de type TEXT
         */
        fun createTextMessage(sender: String, content: String): Message {
            return Message(
                getGlobalId(),
                sender,
                content,
                System.currentTimeMillis().toInt(),
                MessageType.TEXT
            )
        }

        /**
         * Méthode factory pour créer un message contenant un dessin.
         * @param sender Le nom de l'expéditeur
         * @param drawingData Les données du dessin sous forme de ByteArray
         * @return Un nouveau message de type DRAWING
         */
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

        /**
         * Désérialise un message à partir d'un tableau de bytes.
         * @param bytes Les données JSON du message
         * @return Le message désérialisé ou null en cas d'erreur
         */
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