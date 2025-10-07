package it.urronio.mirror.presentation.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.service.SerialService
import it.urronio.mirror.presentation.component.RadioBanner
import it.urronio.mirror.presentation.component.TelemetryDashboard
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
    val telemetry: Telemetry? by viewmodel.telemetry.collectAsState()
    val connected: Boolean by viewmodel.connected.collectAsState()
    val ctx = LocalContext.current
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                val binder = service as SerialService.SerialBinder
                val service = binder.service()
                viewmodel.onServiceBound(device = service.connectedDevice, packet = service.telemetry)
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
        topBar = {
            TopAppBar(
                title = {
                    Text(text = radio?.device?.deviceName ?: "Radio")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
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
            RadioBanner(
                radio = radio,
                open = connected
            ) {
                viewmodel.connect()
            }
            if (telemetry != null)
                TelemetryDashboard(
                    telemetry = telemetry!!
                )
        }
    }
}