package ch.heigvd.iict.dma.pictoAndroidChat

import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

class RoomFragment : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
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
