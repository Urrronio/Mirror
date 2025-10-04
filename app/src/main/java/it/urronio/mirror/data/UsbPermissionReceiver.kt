package it.urronio.mirror.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

class UsbPermissionReceiver(
    private val onPermission: (Boolean, UsbDevice?) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_USB_PERMISSION) {
            synchronized(lock = this) {
                val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
                onPermission(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false), device)
            }
        }
    }
    companion object {
        const val ACTION_USB_PERMISSION = "it.urronio.mirror.USB_PERMISSION"
    }
}