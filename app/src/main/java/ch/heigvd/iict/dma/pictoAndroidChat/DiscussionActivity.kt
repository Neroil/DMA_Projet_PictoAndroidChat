package ch.heigvd.iict.dma.pictoAndroidChat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.dma.pictoAndroidChat.adapter.DiscussionAdapter
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import ch.heigvd.iict.dma.pictoAndroidChat.models.MessageType
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

class DiscussionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: DiscussionAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var inputMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var buttonClear: Button
    private lateinit var eraserToggle: ToggleButton
    private lateinit var canva: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initializeViews()
        setupRecyclerView()
        setupDrawingCanvas()
        setupClickListeners()

        // Example messages
        addTextMessage("User1", "Hello everyone!")
        addTextMessage("User2", "How's everyone doing?")

        // NEW: Add an example drawing message
        addExampleDrawingMessage()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.message_view)
        inputMessage = findViewById(R.id.input_message)
        buttonSend = findViewById(R.id.button_send)
        buttonClear = findViewById(R.id.button_clear)
        eraserToggle = findViewById(R.id.eraser)
        canva = findViewById(R.id.canva)
    }

    private fun setupRecyclerView() {
        messageAdapter = DiscussionAdapter(messages)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupDrawingCanvas() {
        canva.setContent {
            val controller = rememberDrawController()

            DrawBox(
                drawController = controller,
                modifier = Modifier.fillMaxSize(),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let { bitmap ->
                        // Convert the bitmap to base64 and send as drawing message
                        val base64Drawing = bitmapToBase64(bitmap.asAndroidBitmap())
                        addDrawingMessage("CurrentUser", base64Drawing)

                        // Clear the canvas after sending
                        controller.reset()
                    }
                }
            )
        }
    }

    private fun setupClickListeners() {
        buttonSend.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addTextMessage("CurrentUser", messageText)
                inputMessage.text.clear()
            }
        }

        buttonClear.setOnClickListener {
            // Clear the drawing canvas
            // Note: This requires access to the DrawController - you might need to restructure
            // the Compose content to expose the controller
        }

        findViewById<Button>(R.id.button_leave).setOnClickListener {
            finish()
        }
    }

    // NEW: Function to add an example drawing message
    private fun addExampleDrawingMessage() {
        // Create a simple example bitmap drawing (a red circle with "Hi!" text)
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background with white
        canvas.drawColor(Color.WHITE)

        // Draw a red circle
        val circlePaint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
        }
        canvas.drawCircle(100f, 80f, 50f, circlePaint)

        // Draw "Hi!" text in blue
        val textPaint = Paint().apply {
            color = Color.BLUE
            textSize = 30f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Hi!", 100f, 140f, textPaint)

        // Convert to base64 and add as message
        val base64Drawing = bitmapToBase64(bitmap)
        addDrawingMessage("Alice", base64Drawing)

        // Add a text message after the drawing
        addTextMessage("Alice", "I just sent you a drawing! 🎨")
    }

    private fun addTextMessage(sender: String, content: String) {
        val message = Message.createTextMessage(sender, content)
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun addDrawingMessage(sender: String, drawingData: String) {
        val message = Message.createDrawingMessage(sender, drawingData)
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    // Helper function to convert Bitmap to Base64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

// Helper function to convert Bitmap to Base64 (for storing drawings)
fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Extension function to convert ImageBitmap to Android Bitmap
fun androidx.compose.ui.graphics.ImageBitmap.asAndroidBitmap(): Bitmap {
    return createBitmap(width, height).apply {
        copyPixelsFromBuffer(java.nio.IntBuffer.wrap(IntArray(width * height)))
    }
}
