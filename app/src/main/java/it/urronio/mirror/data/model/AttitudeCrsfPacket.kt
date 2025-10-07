package it.urronio.mirror.data.model

data class AttitudeCrsfPacket(
    val pitch: Short,
    val roll: Short,
    val yaw: Short
) : CrsfPacket
