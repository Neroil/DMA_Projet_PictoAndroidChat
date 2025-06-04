package ch.heigvd.iict.dma.pictoAndroidChat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Base64
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.dma.pictoAndroidChat.adapter.DiscussionAdapter
import androidx.lifecycle.Observer
import ch.heigvd.iict.dma.pictoAndroidChat.DiscussionViewModel.ConnectionState
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.DrawController
import io.ak1.drawbox.rememberDrawController
import ch.heigvd.iict.dma.pictoAndroidChat.models.Message
import ch.heigvd.iict.dma.pictoAndroidChat.models.MessageType
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

class DiscussionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: DiscussionAdapter

    private lateinit var nearbyService: NearbyService
    private lateinit var discussionViewModel: DiscussionViewModel
    private lateinit var drawController: DrawController

    private val DRAW_COLOR = Color.Black
    private val ERASER_COLOR = Color.White

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        Log.d("DiscussionActivity", "onCreate")

        initializeViews()
        setupRecyclerView()
        setupDrawingCanvas()
        setupClickListeners()
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
            drawController = rememberDrawController()
            drawController.changeColor(DRAW_COLOR)

            DrawBox(
                drawController = drawController,
                modifier = Modifier.fillMaxSize(),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let {
                        sendBitmap(it.asAndroidBitmap())
                    }
                }
            )
        }


        // Gestion du bouton de retour
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                discussionViewModel.disconnect()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Gestion de la gomme
        findViewById<ToggleButton>(R.id.eraser).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                drawController.changeColor(ERASER_COLOR)
            } else {
                drawController.changeColor(DRAW_COLOR)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService.get(this)
        discussionViewModel = DiscussionViewModel.get()

        findViewById<Button>(R.id.button_send).setOnClickListener {
            val messageBox = findViewById<EditText>(R.id.input_message)

            // Envoie le dessin s'il n'est pas vide
            if (drawController.exportPath().path.isNotEmpty()) {
                drawController.saveBitmap()
            }

            // Envoie le texte s'il n'est pas vide
            if (messageBox.text.isNotEmpty()) {
                Log.d("DiscussionActivity", "Sending message: ${messageBox.text.toString()}")
                discussionViewModel.sendTextMsg(messageBox.text.toString())
            }

            // Efface les inputs
            clear()
        }

        // Observe les changements de connexion
        val connectionObserver = Observer<ConnectionState> { state ->
            Log.d("MainActivity", "Connection state changed to $state")
            when (state) {
                ConnectionState.DISCONNECTED -> {
                    finish()
                }

                else -> {}
            }
        }
        discussionViewModel.connectionState.observe(this, connectionObserver)
    }

    private fun sendBitmap(bitmap: Bitmap){
        discussionViewModel.sendDrawingMsg(bitmap)
    }

    private fun clear(){
        drawController.reset()
        findViewById<EditText>(R.id.input_message).text.clear()
    }

    private fun setupClickListeners() {
        buttonSend.setOnClickListener {

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
