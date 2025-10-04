package it.urronio.mirror.di

import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.RadioRepositoryImpl
import it.urronio.mirror.data.repository.SerialRepository
import it.urronio.mirror.data.repository.SerialRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dataModules = module {
    single<RadioRepository> { RadioRepositoryImpl(manager = get()) }
    single<SerialRepository> { SerialRepositoryImpl(manager = get(), context = androidApplication()) }
}