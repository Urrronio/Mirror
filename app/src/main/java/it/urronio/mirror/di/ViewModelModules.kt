package it.urronio.mirror.di

import it.urronio.mirror.presentation.viewmodel.RadioListViewModel
import it.urronio.mirror.presentation.viewmodel.RadioViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { RadioListViewModel(repository = get(), connManager = get()) }
    viewModel { params ->
        RadioViewModel(application = androidApplication(), repository = get(), name = params.get(), connManager = get())
    }
}