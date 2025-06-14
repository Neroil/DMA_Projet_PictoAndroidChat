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

/**
 * Activité de la salle de discussion. Elle se lance si l'on hébérge une salle ou si on en rejoint une.
 * @author Guillaume Dunant
 * @author Edwin Haeffner
 * @author Arthur Junod
 */

class DiscussionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var discussionAdapter: DiscussionAdapter

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


    /**
     * Met en place la RecyclerView.
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe les messages du ViewModel afin de réagir quand il y en a un nouveau
        discussionViewModel.messages.observe(this) { messages ->
            discussionAdapter = DiscussionAdapter(messages)
            recyclerView.adapter = discussionAdapter

            // permet de défiler automatiquement sur le nouveau message recu
            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    /**
     * Permet de mettre en place le canva qui nous permet de dessiner.
     */
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
    }

    /**
     * Permet d'envoyer une bitmap au ViewModel.
     * @param bitmap La bitmap à envoyer au ViewModel.
     */
    private fun sendBitmap(bitmap: Bitmap){
        discussionViewModel.sendDrawingMsg(bitmap)
    }

    /**
     * Efface tous les input (dessin et texte) de l'utilisateur.
     */
    private fun clear(){
        drawController.reset()
        findViewById<EditText>(R.id.input_message).text.clear()
    }

    /**
     * Met en place les listeners sur tous les boutons que l'on va utiliser.
     */
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

        // Gestion du bouton "clear"
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

        // Gestion du bouton "retour"
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                discussionViewModel.disconnect()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

}
