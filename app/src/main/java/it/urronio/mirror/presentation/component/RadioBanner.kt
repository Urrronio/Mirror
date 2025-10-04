package it.urronio.mirror.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import it.urronio.mirror.data.model.Radio

@Composable
fun RadioBanner(
    modifier: Modifier = Modifier,
    radio: Radio?,
    open: Boolean,
    onClick: () -> Unit
) {
    val dev = radio?.device
    Row {
        Image(
            imageVector = Icons.Default.Radio,
            contentDescription = null
        )
        Column {
            if (dev != null) {
                if (dev.productName != null) {
                    Text(text = dev.productName!!)
                } else {
                    Text(text = dev.deviceName)
                }
            }
        }
        IconButton(
            onClick = {
                onClick()
            }
        ) {
            if (open) {
                Image(
                    imageVector = Icons.Default.UsbOff,
                    contentDescription = "Disconnect radio"
                )
            } else {
                Image(
                    imageVector = Icons.Default.Usb,
                    contentDescription = "Connect to radio"
                )
            }
        }
    }
}