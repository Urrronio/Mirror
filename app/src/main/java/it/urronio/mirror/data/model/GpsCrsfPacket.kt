package it.urronio.mirror.data.model

data class GpsCrsfPacket(
    val latitude: Int,
    val longitude: Int,
    val groundSpeed: Short,
    val groundCourse: Short,
    val altitude: Short,
    val satellite: Byte
)
