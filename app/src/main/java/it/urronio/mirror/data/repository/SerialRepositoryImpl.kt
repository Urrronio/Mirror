package it.urronio.mirror.data.repository

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import it.urronio.mirror.data.CrsfParser
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.Radio
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SerialRepositoryImpl(
    private val manager: UsbManager,
    private val name: String,
) : SerialRepository {
    private val _telemetry: MutableSharedFlow<CrsfPacket> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val telemetry: SharedFlow<CrsfPacket>
        get() = _telemetry.asSharedFlow()
    private var conn: UsbDeviceConnection? = null
    private var radio: Radio? = null
    private val parser = CrsfParser()

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

    override suspend fun open(): Boolean {
        radio = getRadio()
        conn = manager.openDevice(radio!!.device)
        if (conn == null) return false
        val claim = conn!!.claimInterface(
            radio!!.intIntf, true
        )
        if (!claim) return false
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
        return true
    }

    override suspend fun read() {
        val barr = ByteArray(128)
        readLoop@ while (radio != null && conn != null) {
            conn!!.claimInterface(
                radio!!.bulkIntf, true
            )
            val read = conn!!.bulkTransfer(
                radio!!.bulkIn,
                barr,
                barr.size,
                1000
            )
            if (read > 0) {
                parser.put(bytes = barr.copyOf(read))
                parseLoop@ while(true) {
                    val p = parser.nextPacket() ?: break@parseLoop
                    _telemetry.tryEmit(value = p)
                }
            }

        }

    }

    override fun close() {
        conn?.close()
        conn = null
        radio = null
    }
}