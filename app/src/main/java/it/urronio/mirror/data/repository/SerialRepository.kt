package it.urronio.mirror.data.repository

import android.hardware.usb.UsbDevice
import it.urronio.mirror.data.model.Telemetry
import kotlinx.coroutines.flow.SharedFlow

interface SerialRepository {
    fun read()
    fun close()

}