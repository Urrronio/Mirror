package it.urronio.mirror.presentation.viewmodel

import androidx.lifecycle.ViewModel
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.repository.RadioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections.emptyList

class RadioListViewModel(
    private val repository: RadioRepository,
    private val connManager: ConnectionManager
) : ViewModel() {
    private val _radios : MutableStateFlow<List<Radio>> = MutableStateFlow(emptyList())
    val radios : StateFlow<List<Radio>> = _radios.asStateFlow()
    val connected: StateFlow<String?> = connManager.connectedDevice
    init {
        refreshRadios()
    }
    fun refreshRadios() {
        _radios.value = repository.getAttachedRadios()
    }
}