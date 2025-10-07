package it.urronio.mirror.data

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.service.SerialService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectionManager(
) {
}