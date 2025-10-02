package it.urronio.mirror.presentation.screen

import android.text.style.IconMarginSpan
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.presentation.component.RadioCard
import it.urronio.mirror.presentation.viewmodel.RadioListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RadioListScreen(modifier: Modifier = Modifier) {
    val viewmodel: RadioListViewModel = koinViewModel()
    val radios: List<Radio> by viewmodel.radios.collectAsState()
    val ctx = LocalContext.current
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewmodel.refreshRadios()
                }
            ) { Text(text = "Refresh") }
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Fixed(count = 2)
        ) {
            items(radios) { radio ->
                RadioCard(
                    radio = radio
                ) {
                    // handle click
                    Toast.makeText(ctx, "${radio.device.productName} clicked", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}