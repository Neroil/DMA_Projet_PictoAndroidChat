package ch.heigvd.iict.dma.pictoAndroidChat

import android.graphics.Bitmap
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

        recyclerView = findViewById(R.id.message_view)
        setupDrawingCanvas()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe messages and update adapter when they change
        discussionViewModel.messages.observe(this) { messages ->
            messageAdapter = DiscussionAdapter(messages)
            recyclerView.adapter = messageAdapter

            // Auto-scroll to bottom when new messages arrive
            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupDrawingCanvas() {
        findViewById<ComposeView>(R.id.canva).setContent {
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


    }

    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService.get(this)
        discussionViewModel = DiscussionViewModel.get()

        setupClickListeners()
        setupRecyclerView()

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

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            clear()
        }

        // Gestion de la gomme
        findViewById<ToggleButton>(R.id.eraser).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                drawController.changeColor(ERASER_COLOR)
            } else {
                drawController.changeColor(DRAW_COLOR)
            }
        }
    }

}
