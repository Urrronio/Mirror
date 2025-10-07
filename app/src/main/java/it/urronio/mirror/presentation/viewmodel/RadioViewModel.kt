package it.urronio.mirror.presentation.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.UsbPermissionReceiver
import it.urronio.mirror.data.model.AttitudeCrsfPacket
import it.urronio.mirror.data.model.BatteryCrsfPacket
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.GpsCrsfPacket
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.SerialRepository
import it.urronio.mirror.data.service.SerialService
import it.urronio.mirror.presentation.component.TelemetryDashboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class RadioViewModel(
    application: Application,
    private val repository: RadioRepository,
    private val name: String,
    private val manager: UsbManager,
    // private val connManager: ConnectionManager
) : AndroidViewModel(
    application = application
) {
    private val _radio: MutableStateFlow<Radio?> = MutableStateFlow(value = null)
    val radio: StateFlow<Radio?> = _radio
    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
    private val _telemetry: MutableStateFlow<Telemetry?> = MutableStateFlow(value = Telemetry())
    val telemetry: StateFlow<Telemetry?> = _telemetry.asStateFlow()
    private val receiver: UsbPermissionReceiver = UsbPermissionReceiver { granted, device ->
        if (granted && device != null) {
            val i = Intent(
                application,
                SerialService::class.java
            ).apply {
                action = SerialService.ACTION_CONNECT
                putExtra(SerialService.EXTRA_DEVICE_NAME, device.deviceName)
                setPackage(application.packageName)
            }
            ContextCompat.startForegroundService(application, i)
        }
        application.unregisterReceiver(receiver)
    }

    init {
        _radio.value = repository.getRadioByName(name = name)
    }

    fun onServiceBound(
        device: StateFlow<String?>,
        packet: SharedFlow<CrsfPacket?>
    ) {
        viewModelScope.launch {
            device.collect {
                _connected.value = it == name
            }
        }

        viewModelScope.launch {
            packet.collectLatest {
                when (it) {
                    is GpsCrsfPacket -> {
                        _telemetry.value = _telemetry.value?.copy(
                            gps = it
                        )
                    }

                    is BatteryCrsfPacket -> {
                        _telemetry.value = _telemetry.value?.copy(
                            battery = it
                        )
                    }

                    is AttitudeCrsfPacket -> {
                        _telemetry.value = _telemetry.value?.copy(
                            attitude = it
                        )
                    }
                }
            }
        }
    }

    fun onServiceUnbound() {
        _connected.value = false
        _radio.value = null
        _telemetry.value = null
    }

    fun connect() {
        if (_connected.value) {
            disconnect()
            return
        }
        if (_radio.value != null) {
            requestPermission(_radio.value!!.device)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        ContextCompat.registerReceiver(
            application,
            receiver,
            IntentFilter(UsbPermissionReceiver.ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val i = Intent(UsbPermissionReceiver.ACTION_USB_PERMISSION)
        i.setPackage(application.packageName) // explicit intent
        val pi: PendingIntent = PendingIntent.getBroadcast(
            application,
            0,
            i,
            PendingIntent.FLAG_MUTABLE
        )
        manager.requestPermission(device, pi)
    }

    private fun disconnect() {
        val i = Intent(
            application,
            SerialService::class.java
        ).apply {
            action = SerialService.ACTION_DISCONNECT
        }
        ContextCompat.startForegroundService(application, i)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            application.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.d("RadioViewModel", "Tried to unregister a non-registered receiver")
        }
    }
}