package it.urronio.mirror.presentation.component

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.urronio.mirror.data.model.Radio

@Composable
fun RadioCard(
    modifier: Modifier = Modifier,
    radio: Radio,
    connected: Boolean,
    onClick: (UsbDevice) -> Unit
) {
    Card(
        modifier = modifier.clickable {
            onClick(radio.device)
        }
    ) {
        Column {
            val dev = radio.device
            Text(text = dev.deviceName)
            if (dev.manufacturerName != null && dev.productName != null) {
                Text(text = dev.manufacturerName!!)
                Text(text = dev.productName!!)
            } else {
                Text(text = dev.productId.toString())
            }
            if (connected)
                Text(text = "Connected")
        }
    }
}