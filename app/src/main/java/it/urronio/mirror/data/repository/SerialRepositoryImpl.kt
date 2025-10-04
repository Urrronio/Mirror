package it.urronio.mirror.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import it.urronio.mirror.data.UsbPermissionReceiver

class SerialRepositoryImpl(
    private val manager: UsbManager,
    private val context: Context
) : SerialRepository {
    private val devices: HashMap<String, Boolean> = HashMap()
    private val receiver: UsbPermissionReceiver = UsbPermissionReceiver { granted, device ->
        if (device != null) {
            devices[device.deviceName] = granted
            Toast.makeText(context, "Permission granted: $granted on device ${device.deviceName}",
                Toast.LENGTH_SHORT).show()
        }
        context.unregisterReceiver(receiver)
    }
    override fun requestPermission(device: UsbDevice) {
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(UsbPermissionReceiver.ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val i = Intent(UsbPermissionReceiver.ACTION_USB_PERMISSION)
        i.setPackage(context.packageName) // explicit intent
        val pi: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            i,
            PendingIntent.FLAG_MUTABLE
        )
        manager.requestPermission(device, pi)
    }
}