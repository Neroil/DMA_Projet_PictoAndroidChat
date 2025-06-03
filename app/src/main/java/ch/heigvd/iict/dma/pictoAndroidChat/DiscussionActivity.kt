package ch.heigvd.iict.dma.pictoAndroidChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController

class DiscussionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val canva = findViewById<ComposeView>(R.id.canva)
        canva.setContent {
            val controller = rememberDrawController()

            DrawBox(
                drawController = controller, modifier = Modifier.fillMaxSize(),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let {
                        // TODO()
                    }
                }
            )
        }
    }
}