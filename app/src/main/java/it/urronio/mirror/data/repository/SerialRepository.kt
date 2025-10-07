package it.urronio.mirror.data.repository

import android.hardware.usb.UsbDevice
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.Telemetry
import kotlinx.coroutines.flow.SharedFlow

interface SerialRepository {
    val telemetry: SharedFlow<CrsfPacket>
    suspend fun open(): Boolean
    suspend fun read()
    fun close()

}