package it.urronio.mirror.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.UsbPermissionReceiver
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.Telemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SerialRepositoryImpl(
    private val manager: UsbManager,
    private val name: String,
    private val connManager: ConnectionManager,
    private val context: Context
) : SerialRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var conn: UsbDeviceConnection? = null
    private var radio: Radio? = null

    private fun getRadio(): Radio {
        val device = manager.deviceList[name]
        var intIntf: UsbInterface? = null
        var bulkIntf: UsbInterface? = null
        var bulkIn: UsbEndpoint? = null
        var bulkOut: UsbEndpoint? = null
        for (i in 0 until device!!.interfaceCount) {
            if (intIntf != null && bulkIntf != null) break
            val intf = device.getInterface(i)
            if (intIntf == null && intf.interfaceClass == UsbConstants.USB_CLASS_COMM && intf.interfaceSubclass == 0x2) {
                intIntf = intf
            }
            if (bulkIntf == null && intf.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA) {
                bulkIntf = intf
                for (j in 0 until intf.endpointCount) {
                    if (bulkIn != null && bulkOut != null) break
                    val enp = intf.getEndpoint(j)
                    if (enp.direction == UsbConstants.USB_DIR_IN && enp.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        bulkIn = enp
                    }
                    if (enp.direction == UsbConstants.USB_DIR_OUT && enp.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        bulkOut = enp
                    }
                }
            }
        }
        if (intIntf == null || bulkIntf == null || bulkIn == null || bulkOut == null) throw IllegalStateException() // ?
        return Radio(
            device = device,
            intIntf = intIntf,
            bulkIntf = bulkIntf,
            bulkIn = bulkIn,
            bulkOut = bulkOut
        )
    }

    override fun read() {
        scope.launch {
            radio = getRadio()
            conn = manager.openDevice(radio!!.device)
            if (conn == null) return@launch
            connManager.setConnectedDevice(radio!!.device.deviceName)
            conn!!.claimInterface(
                radio!!.intIntf, true
            )
            val buff = ByteBuffer.allocate(7)
            buff.order(ByteOrder.LITTLE_ENDIAN)
            buff.putInt(115200)
            buff.put(0x0)
            buff.put(0x0)
            buff.put(0x8)
            conn!!.controlTransfer(
                0x21, // host to device
                0x20, // bRequest
                0x0,
                radio!!.intIntf.id,
                buff.array(),
                buff.capacity(),
                1000
            )
            val barr = ByteArray(128)
            while (radio != null && conn != null) {
                conn!!.claimInterface(
                    radio!!.bulkIntf, true
                )
                conn!!.bulkTransfer(
                    radio!!.bulkIn,
                    barr,
                    barr.size,
                    1000
                )
                connManager.postData() // send parsed packed for the manager to handle
                /* withContext(Dispatchers.Main) {
                    if (barr.contains(0xC8.toByte()) || barr.contains(0xEA.toByte()))
                        Toast.makeText(
                            context, "Sync byte found",
                            Toast.LENGTH_SHORT
                        ).show()
                } */
            }
        }
    }

    override fun close() {
        conn?.close()
        connManager.clear()
        conn = null
        radio = null
    }
}