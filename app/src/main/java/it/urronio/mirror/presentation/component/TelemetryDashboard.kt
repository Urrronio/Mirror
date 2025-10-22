package it.urronio.mirror.presentation.component

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dellisd.spatialk.geojson.Position
import it.urronio.mirror.data.model.GpsCrsfPacket
import it.urronio.mirror.data.model.Telemetry
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import java.nio.file.WatchEvent

@Composable
fun TelemetryDashboard(
    modifier: Modifier = Modifier,
    telemetry: Telemetry,
    isSpoofing: Boolean,
    onSpoofingToggleClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val cameraState = rememberCameraState(
        /* firstPosition = CameraPosition(
            target = Position(
                longitude = 9.12,
                latitude = 39.22
            )
        ) */
    )
    cameraState.position = CameraPosition(
        // Cagliari
        target = Position(
            longitude = 9.12,
            latitude = 39.22
        )
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Battery", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = "Voltage", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "Voltage", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val voltage = telemetry.battery?.voltage?.let {
                            "${it / 10.0f} V"
                        } ?: "? V"
                        Text(text = voltage, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Power, contentDescription = "Used capacity", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "Used Capacity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val usedCap =
                            telemetry.battery?.usedCap?.let {
                                "${it / 10.0f} mAh"
                            } ?: "? mAh"
                        Text(text = usedCap, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "GPS", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Coordinates", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "Latitude", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${telemetry.gps?.latitude ?: '?'}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Longitude", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${telemetry.gps?.longitude ?: '?'}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Height, contentDescription = "Altitude", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "Altitude", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${telemetry.gps?.altitude ?: '?'} m", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Card(
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "GPS Spoofing", style = MaterialTheme.typography.titleMedium)
                    IconButton(
                        onClick = {
                            onSpoofingToggleClick()
                        }
                    ) {
                        Icon(
                            imageVector = if (isSpoofing) {
                                Icons.Default.GpsOff
                            } else {
                                Icons.Default.GpsFixed
                            },
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Card {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val style = """
                            {
                            "version": 8,
                            "name": "OpenStreetMap Raster Tiles",
                            "sources": {
                                    "osm-raster-source": {
                                        "type": "raster",
                                        "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                                        "tileSize": 256,
                                        "maxzoom": 19,
                                        "attribution": "OpenStreetMap contributors"
                                    }
                                },
                            "layers": [
                                    {
                                    "id": "osm-raster-layer",
                                    "type": "raster",
                                    "source": "osm-raster-source"
                                    }
                                ]
                            }
                        """.trimIndent()
                        val gestures = GestureOptions(
                            isRotateEnabled = false,
                            isScrollEnabled = false,
                            isTiltEnabled = false,
                            isZoomEnabled = false,
                            isDoubleTapEnabled = false,
                            isQuickZoomEnabled = false,
                        )
                        val options = MapOptions(gestureOptions = gestures)
                        MaplibreMap(
                            modifier = Modifier.fillMaxSize(),
                            baseStyle = BaseStyle.Json(json = style),
                            cameraState = cameraState,
                            options = options
                        ) {

                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TelemetryDashboardPreview() {
    TelemetryDashboard(
        telemetry = Telemetry(),
        isSpoofing = false
    ) { }
}