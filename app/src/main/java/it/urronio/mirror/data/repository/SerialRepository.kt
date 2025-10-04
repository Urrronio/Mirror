package it.urronio.mirror.data.repository

import android.hardware.usb.UsbDevice

interface SerialRepository {
    fun requestPermission(device: UsbDevice)

}