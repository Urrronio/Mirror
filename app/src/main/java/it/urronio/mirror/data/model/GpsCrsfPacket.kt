package it.urronio.mirror.data.model

data class GpsCrsfPacket(
    val latitude: Int,
    val longitude: Int,
    val groundSpeed: Short,
    val heading: Short,
    val altitude: Short,
    val satellites: Byte
) : CrsfPacket
