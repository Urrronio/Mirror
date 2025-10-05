package it.urronio.mirror.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.SerialRepository
import it.urronio.mirror.data.service.SerialService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioViewModel(
    application: Application,
    private val repository: RadioRepository,
    private val name: String,
    private val connManager: ConnectionManager
): AndroidViewModel(
    application = application
) {
    private val _radio: MutableStateFlow<Radio?> = MutableStateFlow(value = null)
    val radio: StateFlow<Radio?> = _radio
    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
    val telemetry: StateFlow<Telemetry?> = connManager.telemetry
    init {
        _radio.value = repository.getRadioByName(name = name)
        viewModelScope.launch {
            connManager.connectedDevice.collect {
                _connected.value = it == name
            }
        }
    }
    fun connect() {
        if (_connected.value) {
            disconnect()
            return
        }
        if (_radio.value != null) {
            repository.requestPermission(_radio.value!!.device)
        }
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
}