package it.urronio.mirror.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import it.urronio.mirror.data.model.AttitudeCrsfPacket
import it.urronio.mirror.data.model.BatteryCrsfPacket
import it.urronio.mirror.data.model.ChannelsCrsfPacket
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.GpsCrsfPacket
import it.urronio.mirror.data.repository.LocationRepository
import it.urronio.mirror.data.repository.SerialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf

class SerialService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var readJob: Job? = null
    private var forwardJob: Job? = null
    private var mockJob: Job? = null
    private lateinit var serial: SerialRepository
    private lateinit var location: LocationRepository
    private val binder: SerialBinder = SerialBinder()
    private val _connectedDevice: MutableStateFlow<String?> = MutableStateFlow(value = null)
    val connectedDevice: StateFlow<String?> = _connectedDevice.asStateFlow()
    private val _telemetry: MutableSharedFlow<CrsfPacket?> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _isMocking: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val isMocking: StateFlow<Boolean> = _isMocking.asStateFlow()
    val telemetry: SharedFlow<CrsfPacket?> = _telemetry.asSharedFlow()
    override fun onBind(intent: Intent?): IBinder? = binder
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val name: String? = intent.getStringExtra(EXTRA_DEVICE_NAME)
                name?.let {
                    serial = getKoin().get(parameters = { parametersOf(name) })
                    location = getKoin().get()
                    startForeground(
                        NOTIFICATION_ID,
                        notification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                    readJob = scope.launch {
                        val open = serial.open()
                        if (!open) {
                            serial.close()
                            _connectedDevice.value = null
                            readJob?.cancel()
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            return@launch
                        }
                        _connectedDevice.value = name
                        serial.read()
                    }
                    forwardJob = scope.launch {
                        serial.telemetry.collect {
                            Log.d(
                                "SerialService",
                                when (it) {
                                    is BatteryCrsfPacket -> "BatteryPacket"
                                    is AttitudeCrsfPacket -> "AttitudePacket"
                                    is GpsCrsfPacket -> "GpsPacket"
                                    is ChannelsCrsfPacket -> "ChannelsPacket"
                                    else -> "Other packet"
                                }
                            )
                            _telemetry.tryEmit(value = it)
                        }
                    }
                    mockJob = scope.launch {
                        serial.telemetry
                            .filter {
                                it is GpsCrsfPacket
                            }
                            .map {
                                it as GpsCrsfPacket
                            }
                            .collectLatest {
                                /* location.setLocation(
                                    // illegalargumentexception missing timestamp or accuracy on location object
                                    Location(location.provider).apply {
                                        latitude = it.latitude.toDouble()
                                        longitude = it.longitude.toDouble()
                                        altitude = it.altitude.toDouble()
                                    }
                                ) */
                            }
                    }
                }
            }
            ACTION_SPOOF -> {
                // both starts connection and spoofing
                val name: String? = intent.getStringExtra(EXTRA_DEVICE_NAME)
            }
            ACTION_DISCONNECT -> {
                serial.close()
                _connectedDevice.value = null
                readJob?.cancel()
                forwardJob?.cancel()
                mockJob?.cancel()
                stopSpoofing()
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
        return START_NOT_STICKY
    }

    fun spoof(device: String) {
        if (_isMocking.value) stopSpoofing()
        else startSpoofing(device = device)
    }
    private fun startSpoofing(
        device: String
    ) {
        if (device != _connectedDevice.value) return
        if (location.start()) _isMocking.value = true
    }
    private fun stopSpoofing() {
        location.stop()
        _isMocking.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        serial.close()
        _connectedDevice.value = null
        stopSpoofing()
        scope.cancel()
    }

    private fun notification(): Notification {
        // minimum api is greater than O: no need to surround with if
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Mirror USB Connection",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Radio connected")
            .setContentText("Reading data from radio...")
            .setSmallIcon(0)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    inner class SerialBinder : Binder() {
        fun service(): SerialService = this@SerialService
    }

    companion object {
        const val ACTION_CONNECT = "it.urronio.mirror.action.CONNECT"
        const val ACTION_SPOOF = "it.urronio.mirror.action.SPOOF"
        const val ACTION_DISCONNECT = "it.urronio.mirror.action.DISCONNECT"
        const val EXTRA_DEVICE_NAME = "it.urronio.mirror.extra.DEVICE_NAME"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SerialServiceChannel"
    }
}