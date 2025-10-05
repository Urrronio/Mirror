package it.urronio.mirror.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.presentation.component.RadioBanner
import it.urronio.mirror.presentation.viewmodel.RadioViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    name: String,
    onNavigateUp: () -> Unit
) {
    val viewmodel: RadioViewModel = koinViewModel(parameters = { parametersOf(name) })
    val radio: Radio? by viewmodel.radio.collectAsState()
    val connected: Boolean by viewmodel.connected.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = radio?.device?.deviceName ?: "Radio")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // release connection ?
                            // viewmodel.disconnect()
                            onNavigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            RadioBanner(radio = radio, open = connected) {
                // handle connect/disconnect button
                viewmodel.connect()
            }
        }
    }
}