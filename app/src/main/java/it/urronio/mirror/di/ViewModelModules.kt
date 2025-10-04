package it.urronio.mirror.di

import it.urronio.mirror.presentation.viewmodel.RadioListViewModel
import it.urronio.mirror.presentation.viewmodel.RadioViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { RadioListViewModel(repository = get()) }
    viewModel { params ->
        RadioViewModel(repository = get(), serial = get(), name = params.get())
    }
}