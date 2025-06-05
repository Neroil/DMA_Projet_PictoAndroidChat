package ch.heigvd.iict.dma.pictoAndroidChat.models

/**
 * Modèle de données pour une discussion de chat.
 * Gère la liste des messages et les informations de l'utilisateur local.
 *
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 */
class DiscussionModel {
    private val messages = mutableListOf<Message>()
    var localUserInfo: LocalUserInfo? = null

    /**
     * Ajoute un nouveau message à la discussion.
     * @param message Le message à ajouter à la liste
     */
    fun addMessage(message: Message) {
        messages.add(message)
    }

    /**
     * Retourne tous les messages de la discussion.
     * @return Une copie de la liste des messages
     */
    fun getAllMessages(): List<Message> {
        return messages.toList()
    }

    /**
     * Supprime tous les messages de la discussion.
     */
    fun clearMessages() {
        messages.clear()
    }
}