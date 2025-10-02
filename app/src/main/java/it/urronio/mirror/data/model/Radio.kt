package it.urronio.mirror.data.model

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface

data class Radio(
    val device: UsbDevice,
    // Interrupt interface
    val intIntf: UsbInterface,
    // Bulk transfer interface
    val bulkIntf: UsbInterface,
    // Bulk endpoint IN direction
    val bulkIn: UsbEndpoint,
    // Bulk endpoint OUT direction
    val bulkOut: UsbEndpoint
)
