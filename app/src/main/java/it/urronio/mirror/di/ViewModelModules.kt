package it.urronio.mirror.di

import it.urronio.mirror.presentation.viewmodel.RadioListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { RadioListViewModel(repository = get()) }
}