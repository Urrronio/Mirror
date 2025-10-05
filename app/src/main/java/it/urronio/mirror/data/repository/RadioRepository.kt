package it.urronio.mirror.data.repository

import android.hardware.usb.UsbDevice
import it.urronio.mirror.data.model.Radio

interface RadioRepository {
    fun getAttachedRadios() : List<Radio>
    fun getRadioByName(name: String): Radio
    fun requestPermission(device: UsbDevice)
}