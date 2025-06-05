package ch.heigvd.iict.dma.pictoAndroidChat.adapter

import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.dma.pictoAndroidChat.R
import ch.heigvd.iict.dma.pictoAndroidChat.models.MessageType

/**
 * Adaptateur RecyclerView pour afficher les messages dans une discussion.
 * Gère l'affichage de deux types de messages : texte et dessin.
 *
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 *
 * @param messages La liste des messages à afficher
 */
class DiscussionAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        /** Type de vue pour les messages texte */
        private const val VIEW_TYPE_TEXT = 1
        /** Type de vue pour les messages dessin */
        private const val VIEW_TYPE_DRAWING = 2
    }

    /**
     * Détermine le type de vue à utiliser pour un message donné.
     * @param position La position du message dans la liste
     * @return Le type de vue (VIEW_TYPE_TEXT ou VIEW_TYPE_DRAWING)
     */
    override fun getItemViewType(position: Int): Int {
        return when (messages[position].getMessageType()) {
            MessageType.TEXT -> VIEW_TYPE_TEXT
            MessageType.DRAWING -> VIEW_TYPE_DRAWING
        }
    }

    /**
     * Crée un nouveau ViewHolder selon le type de vue requis.
     * @param parent Le ViewGroup parent
     * @param viewType Le type de vue à créer
     * @return Le ViewHolder approprié (TextMessageViewHolder ou DrawingMessageViewHolder)
     * @throws IllegalArgumentException Si le type de vue est invalide
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("Adapter", "adding message of type : $viewType")
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_text_message, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_DRAWING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_drawing_message, parent, false)
                DrawingMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    /**
     * Lie les données d'un message à son ViewHolder.
     * @param holder Le ViewHolder à lier
     * @param position La position du message dans la liste
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bind(message)
            is DrawingMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * Retourne le nombre total de messages.
     * @return Le nombre d'éléments dans la liste
     */
    override fun getItemCount(): Int = messages.size

    /**
     * ViewHolder pour l'affichage des messages texte.
     * Gère l'affichage de l'expéditeur, du contenu et de l'horodatage.
     */
    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.tv_sender)
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_content)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_timestamp)

        /**
         * Lie les données d'un message texte aux vues.
         * @param message Le message à afficher
         */
        fun bind(message: Message) {
            senderTextView.text = message.getSender()
            contentTextView.text = message.getContent()
            timestampTextView.text = formatTimestamp(message.getTimestamp())
        }

        /**
         * Formate un timestamp en chaîne de caractères lisible.
         * @param timestamp Le timestamp à formater
         * @return L'heure formatée au format HH:mm
         */
        private fun formatTimestamp(timestamp: Int): String {
            val date = java.util.Date(timestamp.toLong())
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
    }

    /**
     * ViewHolder pour l'affichage des messages contenant des dessins.
     * Gère l'affichage de l'expéditeur, du dessin décodé et de l'horodatage.
     */
    class DrawingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.tv_sender)
        private val drawingImageView: ImageView = itemView.findViewById(R.id.iv_drawing)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_timestamp)

        /**
         * Lie les données d'un message dessin aux vues.
         * Décode les données Base64 pour afficher l'image.
         * @param message Le message contenant le dessin à afficher
         */
        fun bind(message: Message) {
            senderTextView.text = message.getSender()
            timestampTextView.text = formatTimestamp(message.getTimestamp())

            // Charge le dessin à partir de la chaîne base64 ou des données de dessin
            message.getDrawingData()?.let { drawingData ->
                try {
                    // Si c'est un bitmap encodé en base64
                    val decodedBytes = Base64.decode(drawingData, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    drawingImageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // Gère l'erreur - peut définir une image de remplacement
                }
            }
        }

        /**
         * Formate un timestamp en chaîne de caractères lisible.
         * @param timestamp Le timestamp à formater
         * @return L'heure formatée au format HH:mm
         */
        private fun formatTimestamp(timestamp: Int): String {
            val date = java.util.Date(timestamp.toLong())
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
    }
}