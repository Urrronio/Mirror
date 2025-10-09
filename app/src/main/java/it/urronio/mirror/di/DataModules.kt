package it.urronio.mirror.di

import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.repository.LocationRepository
import it.urronio.mirror.data.repository.LocationRepositoryImpl
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.RadioRepositoryImpl
import it.urronio.mirror.data.repository.SerialRepository
import it.urronio.mirror.data.repository.SerialRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModules = module {
    single<ConnectionManager> { ConnectionManager() }
    single<RadioRepository> { RadioRepositoryImpl(manager = get(), context = androidContext()) }
    factory<SerialRepository> { params ->
        SerialRepositoryImpl(usbManager = get(), name = params.get())
    }
    factory<LocationRepository> {
        LocationRepositoryImpl(locationManager = get())
    }
}