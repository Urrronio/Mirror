package it.urronio.mirror.data

import it.urronio.mirror.data.model.Telemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionManager {
    private val _connectedDevice = MutableStateFlow<String?>(value = null)
    val connectedDevice: StateFlow<String?> = _connectedDevice
    private val _telemetry = MutableStateFlow<Telemetry?>(value = null)
    val telemetry: StateFlow<Telemetry?> = _telemetry
    fun setConnectedDevice(deviceName: String) {
        _connectedDevice.value = deviceName
    }
    fun postData() {

    }
    fun clear() {
        _connectedDevice.value = null
        _telemetry.value = null
    }
}