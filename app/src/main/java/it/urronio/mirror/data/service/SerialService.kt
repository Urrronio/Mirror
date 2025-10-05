package it.urronio.mirror.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.repository.SerialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class SerialService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var serial: SerialRepository
    private val manager: ConnectionManager by inject()
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val name: String? = intent.getStringExtra(EXTRA_DEVICE_NAME)
                name?.let {
                    serial = getKoin().get(parameters = { parametersOf(name) })
                    startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
                    scope.launch {
                        serial.read()
                    }
                }
            }
            ACTION_DISCONNECT -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serial.close()
        scope.cancel()
        manager.clear()
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
    companion object {
        const val ACTION_CONNECT = "it.urronio.mirror.action.CONNECT"
        const val ACTION_DISCONNECT = "it.urronio.mirror.action.DISCONNECT"
        const val EXTRA_DEVICE_NAME = "it.urronio.mirror.extra.DEVICE_NAME"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SerialServiceChannel"
    }
}