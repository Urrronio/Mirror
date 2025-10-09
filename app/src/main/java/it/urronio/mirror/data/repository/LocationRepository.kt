package it.urronio.mirror.data.repository

import android.location.Location

interface LocationRepository {
    val provider: String
    /**
     * @return True if adding the test provider ended with success, false otherwise.
     * */
    fun start(): Boolean
    fun setLocation(location: Location)
    fun stop()
}