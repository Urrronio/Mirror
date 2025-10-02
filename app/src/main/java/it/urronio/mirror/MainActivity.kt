package it.urronio.mirror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.urronio.mirror.presentation.screen.RadioListScreen
import it.urronio.mirror.ui.theme.MirrorTheme

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        // TODO: register receiver
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MirrorTheme {
                RadioListScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // TODO: unregister receiver
    }
}
