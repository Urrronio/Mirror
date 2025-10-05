package it.urronio.mirror.data.model

data class BatteryCrsfPacket(
    val voltage: Short,
    val current: Short,
    val usedCap: Int,
    val remBatt: Byte
)
