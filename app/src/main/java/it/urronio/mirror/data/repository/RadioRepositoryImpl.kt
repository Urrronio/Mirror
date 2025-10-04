package it.urronio.mirror.data.repository

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import androidx.collection.mutableObjectListOf
import it.urronio.mirror.data.model.Radio

class RadioRepositoryImpl(
    private val manager: UsbManager
) : RadioRepository {
    override fun getAttachedRadios(): List<Radio> {
        return getCdcRadios()
    }

    override fun getRadioByName(name: String): Radio {
        val devices = manager.deviceList
        val device = devices[name]
        if (device == null) {
            throw IllegalArgumentException() // ?
        }
        var intIntf: UsbInterface? = null
        var bulkIntf: UsbInterface? = null
        var bulkIn: UsbEndpoint? = null
        var bulkOut: UsbEndpoint? = null
        for (i in 0 until device.interfaceCount) {
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

    private fun getCdcRadios(): List<Radio> {
        val devices = manager.deviceList
        val cdcRadios: MutableList<Radio> = mutableListOf()
        for (device in devices.values) {
            var intIntf: UsbInterface? = null
            var bulkIntf: UsbInterface? = null
            var bulkIn: UsbEndpoint? = null
            var bulkOut: UsbEndpoint? = null
            for (i in 0 until device.interfaceCount) {
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
            if (intIntf == null || bulkIntf == null || bulkIn == null || bulkOut == null) continue
            cdcRadios.add(
                Radio(
                    device = device,
                    intIntf = intIntf,
                    bulkIntf = bulkIntf,
                    bulkIn = bulkIn,
                    bulkOut = bulkOut
                )
            )
        }
        return cdcRadios
    }
}