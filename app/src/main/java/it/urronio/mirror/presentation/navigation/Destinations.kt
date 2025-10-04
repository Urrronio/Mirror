package it.urronio.mirror.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable
object RadioList

@Serializable
data class Radio(
    val deviceName: String
)