package ch.heigvd.iict.dma.pictoAndroidChat.adapter

import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.dma.pictoAndroidChat.R
import ch.heigvd.iict.dma.pictoAndroidChat.models.MessageType

class DiscussionAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_DRAWING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].getMessageType()) {
            MessageType.TEXT -> VIEW_TYPE_TEXT
            MessageType.DRAWING -> VIEW_TYPE_DRAWING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bind(message)
            is DrawingMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // ViewHolder for text messages
    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.tv_sender)
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_content)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(message: Message) {
            senderTextView.text = message.getSender()
            contentTextView.text = message.getContent()
            timestampTextView.text = formatTimestamp(message.getTimestamp())
        }

        private fun formatTimestamp(timestamp: Int): String {
            val date = java.util.Date(timestamp.toLong())
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
    }

    // ViewHolder for drawing messages
    class DrawingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.tv_sender)
        private val drawingImageView: ImageView = itemView.findViewById(R.id.iv_drawing)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(message: Message) {
            senderTextView.text = message.getSender()
            timestampTextView.text = formatTimestamp(message.getTimestamp())

            // Load drawing from base64 string or drawing data
            message.getDrawingData()?.let { drawingData ->
                try {
                    // If it's base64 encoded bitmap
                    val decodedBytes = Base64.decode(drawingData, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    drawingImageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // Handle error - maybe set a placeholder image
                }
            }
        }

        private fun formatTimestamp(timestamp: Int): String {
            val date = java.util.Date(timestamp.toLong())
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
    }
}