package it.urronio.mirror.presentation.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import it.urronio.mirror.data.UsbDeviceDetachedReceiver
import it.urronio.mirror.data.UsbPermissionReceiver
import it.urronio.mirror.data.model.AttitudeCrsfPacket
import it.urronio.mirror.data.model.BatteryCrsfPacket
import it.urronio.mirror.data.model.ChannelsCrsfPacket
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.GpsCrsfPacket
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.RemoteRelatedCrsfPacket
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.service.SerialService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RadioViewModel(
    application: Application,
    private val repository: RadioRepository,
    private val name: String,
    private val manager: UsbManager,
) : AndroidViewModel(
    application = application
) {
    private val _radio: MutableStateFlow<Radio?> = MutableStateFlow(value = null)
    val radio: StateFlow<Radio?> = _radio
    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
    private val _spoofing: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val spoofing: StateFlow<Boolean> = _spoofing.asStateFlow()
    private val _telemetry: MutableStateFlow<Telemetry?> = MutableStateFlow(value = Telemetry())
    val telemetry: StateFlow<Telemetry?> = _telemetry.asStateFlow()
    private val permissionReceiver: UsbPermissionReceiver = UsbPermissionReceiver { granted, device ->
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
        application.unregisterReceiver(permissionReceiver)
    }

    init {
        _radio.value = repository.getRadioByName(name = name)
    }
    fun detached(device: String) {
        if (device == name) {
            _connected.value = false
            _radio.value = null
            // should trigger a navigation pop
        }
    }

    fun onServiceBound(
        device: StateFlow<String?>,
        packet: SharedFlow<CrsfPacket?>,
        mocking: StateFlow<Boolean>,
    ) {
        viewModelScope.launch {
            device.collect {
                _connected.value = it == name
            }
        }
        viewModelScope.launch {
            mocking.collect {
                _spoofing.value = it
            }
        }

        viewModelScope.launch {
            packet.collect {
                when (it) {
                    is GpsCrsfPacket -> {
                        Log.d("RadioViewModel", "Gps packed received")
                        _telemetry.value = _telemetry.value?.copy(
                            gps = it
                        )
                    }

                    is BatteryCrsfPacket -> {
                        Log.d("RadioViewModel", "Battery packed received")
                        _telemetry.value = _telemetry.value?.copy(
                            battery = it
                        )
                    }

                    is AttitudeCrsfPacket -> {
                        Log.d("RadioViewModel", "Attitude packed received")
                        _telemetry.value = _telemetry.value?.copy(
                            attitude = it
                        )
                    }

                    is ChannelsCrsfPacket -> {
                        Log.d("RadioViewModel", "Channels packed received")
                        _telemetry.value = _telemetry.value?.copy(
                            channels = it
                        )
                    }

                    is RemoteRelatedCrsfPacket -> {
                        // Log.d("RadioViewModel", "Remote related packed received")
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
            permissionReceiver,
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
            application.unregisterReceiver(permissionReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d("RadioViewModel", "Tried to unregister a non-registered receiver")
        }
    }
}