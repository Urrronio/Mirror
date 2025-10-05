package it.urronio.mirror.data.model

// github.com/crsf-wg/crsf/wiki/Packet-Types
// github.com/tbs-fpv/tbs-crsf-spec/blob/main/crsf.md
data class Telemetry(
    val gps: GpsCrsfPacket,
    val battery: BatteryCrsfPacket,
    val attitude: AttitudeCrsfPacket
)
