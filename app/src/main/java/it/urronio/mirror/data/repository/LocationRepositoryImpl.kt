package it.urronio.mirror.data.repository

import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build

class LocationRepositoryImpl(
    private val locationManager: LocationManager
) : LocationRepository {
    override val provider: String
        get() = LocationManager.GPS_PROVIDER
    override fun start(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    provider,
                    ProviderProperties.Builder()
                        .setHasAltitudeSupport(true)
                        // other properties are false by default
                        .build()
                )
            } else {
                locationManager.addTestProvider(
                    provider,
                    false,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    Criteria.POWER_LOW, // using ProviderProperties requires API gt S, already handled above
                    Criteria.ACCURACY_FINE // using ProviderProperties requires API gt S, already handled above
                )
            }
            locationManager.setTestProviderEnabled(provider, true)
        } catch (e: SecurityException) {
            return false
        }
        return true
    }

    override fun setLocation(location: Location) {
        try {
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.setTestProviderLocation(provider, location)
            }
        } catch (e: SecurityException) {

        }
    }

    override fun stop() {
        try {
            locationManager.setTestProviderEnabled(provider, false)
            locationManager.removeTestProvider(provider)
        } catch (e: SecurityException) {

        }
    }

}