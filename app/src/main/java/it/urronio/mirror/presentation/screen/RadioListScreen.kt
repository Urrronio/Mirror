package it.urronio.mirror.presentation.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.style.IconMarginSpan
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.service.SerialService
import it.urronio.mirror.presentation.component.RadioCard
import it.urronio.mirror.presentation.viewmodel.RadioListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioListScreen(
    modifier: Modifier = Modifier,
    onNavigateToRadio: (String) -> Unit
) {
    val viewmodel: RadioListViewModel = koinViewModel()
    val radios: List<Radio> by viewmodel.radios.collectAsState()
    val connected: String? by viewmodel.connected.collectAsState()
    val ctx = LocalContext.current
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                val binder = service as SerialService.SerialBinder
                viewmodel.onServiceBound(binder.service().connectedDevice)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                viewmodel.onServiceUnbound()
            }

        }
    }
    DisposableEffect(key1 = Unit) {
        ctx.bindService(
            Intent(ctx, SerialService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        onDispose {
            ctx.unbindService(connection)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Radios")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewmodel.refreshRadios()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh attached radios"
                )
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Fixed(count = 2)
        ) {
            items(radios) { radio ->
                RadioCard(
                    radio = radio,
                    connected = radio.device.deviceName == connected
                ) {
                    Toast.makeText(ctx, "${radio.device.productName} clicked", Toast.LENGTH_SHORT)
                        .show()
                    onNavigateToRadio(radio.device.deviceName)
                }
            }
        }
    }

}