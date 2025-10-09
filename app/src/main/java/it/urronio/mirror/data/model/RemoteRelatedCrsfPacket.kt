package it.urronio.mirror.data.model

data class RemoteRelatedCrsfPacket(
    val dest: Byte,
    var orig: Byte
): CrsfPacket
