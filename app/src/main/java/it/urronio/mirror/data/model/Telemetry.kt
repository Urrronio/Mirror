package it.urronio.mirror.data.model


data class Telemetry(
    val gps: GpsCrsfPacket? = null,
    val battery: BatteryCrsfPacket? = null,
    val attitude: AttitudeCrsfPacket? = null
)
