package it.urronio.mirror.di

import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.RadioRepositoryImpl
import org.koin.dsl.module

val dataModules = module {
    single<RadioRepository> { RadioRepositoryImpl(manager = get()) }
}