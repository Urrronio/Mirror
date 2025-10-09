package it.urronio.mirror.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.urronio.mirror.data.model.Telemetry

@Composable
fun TelemetryDashboard(
    modifier: Modifier = Modifier,
    telemetry: Telemetry,
    isSpoofing: Boolean,
    onSpoofingToggleClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) { // battery row
            Card {
                Column {
                    Text(text = "Battery voltage")
                    Text(text = telemetry.battery?.voltage?.toString() ?: "?")
                }
            }
            Card {
                Column {
                    Text(text = "Used capacity")
                    Text(text = telemetry.battery?.usedCap?.toString() ?: "?")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) { // gps row
            Card {
                Column {
                    Text(text = "Latitude")
                    Text(text = telemetry.gps?.latitude?.toString() ?: "?")
                }
            }
            Card {
                Column {
                    Text(text = "Longitude")
                    Text(text = telemetry.gps?.longitude?.toString() ?: "?")
                }
            }
            Card {
                Column {
                    Text(text = "Altitude")
                    Text(text = telemetry.gps?.altitude?.toString() ?: "?")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) { // map row
            Column {
                Row {
                    Text(text = "Gps spoofing")
                    Button(
                        onClick = {
                            onSpoofingToggleClick()
                        }
                    ) {
                        if (isSpoofing)
                            Text(text = "ON")
                        else
                            Text(text = "OFF")
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "This will contain the map")
                }
            }
        }
    }
}