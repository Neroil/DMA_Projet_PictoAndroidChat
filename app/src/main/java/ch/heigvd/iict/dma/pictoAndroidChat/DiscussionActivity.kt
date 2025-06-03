package ch.heigvd.iict.dma.pictoAndroidChat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import ch.heigvd.iict.dma.pictoAndroidChat.DiscussionViewModel.ConnectionState
import ch.heigvd.iict.dma.pictoAndroidChat.services.NearbyService
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.DrawController
import io.ak1.drawbox.rememberDrawController

class DiscussionActivity : AppCompatActivity() {

    private lateinit var nearbyService: NearbyService
    private lateinit var discussionViewModel: DiscussionViewModel
    private lateinit var drawController: DrawController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        Log.d("DiscussionActivity", "onCreate")

        val canva = findViewById<ComposeView>(R.id.canva)
        canva.setContent {
            drawController = rememberDrawController()

            DrawBox(
                drawController = drawController, modifier = Modifier.fillMaxSize(),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let {
                        // TODO()
                    }
                }
            )
        }

        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                discussionViewModel.disconnect()
            }
        }
        onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    override fun onStart() {
        super.onStart()
        nearbyService = NearbyService.get(this)
        discussionViewModel = DiscussionViewModel.get()

        findViewById<Button>(R.id.button_send).setOnClickListener {
            val messageBox = findViewById<EditText>(R.id.input_message)
            Log.d("DiscussionActivity", "Sending message: ${messageBox.text.toString()}")
            discussionViewModel.sendMessage(messageBox.text.toString())
            messageBox.text.clear()

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
}