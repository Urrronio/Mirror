package it.urronio.mirror.presentation.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.text.style.IconMarginSpan
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import it.urronio.mirror.data.UsbDeviceDetachedReceiver
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
    val detachedReceiver: UsbDeviceDetachedReceiver = remember {
        UsbDeviceDetachedReceiver { device ->
            viewmodel.refreshRadios()
        }
    }
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
    LaunchedEffect(key1 = Unit) {
        viewmodel.refreshRadios()
    }
    DisposableEffect(key1 = Unit) {
        ctx.bindService(
            Intent(ctx, SerialService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        ctx.registerReceiver(detachedReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
        onDispose {
            ctx.unbindService(connection)
            ctx.unregisterReceiver(detachedReceiver)
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            if (radios.isNotEmpty()) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize().padding(paddingValues = padding),
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(radios) { radio ->
                        RadioCard(
                            radio = radio,
                            connected = radio.device.deviceName == connected
                        ) {
                            onNavigateToRadio(radio.device.deviceName)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues = padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No radios attached")
                }
            }
    }

}