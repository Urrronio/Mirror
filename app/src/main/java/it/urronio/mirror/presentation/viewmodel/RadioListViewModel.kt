package it.urronio.mirror.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.urronio.mirror.data.ConnectionManager
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.model.Telemetry
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.service.SerialService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Collections.emptyList

class RadioListViewModel(
    private val repository: RadioRepository,
) : ViewModel() {
    private val _radios : MutableStateFlow<List<Radio>> = MutableStateFlow(emptyList())
    val radios : StateFlow<List<Radio>> = _radios.asStateFlow()
    private val _connected: MutableStateFlow<String?> = MutableStateFlow(value = null)
    val connected: StateFlow<String?> = _connected.asStateFlow()
    fun refreshRadios() {
        _radios.value = repository.getAttachedRadios()
    }
    fun onServiceBound(device: StateFlow<String?>) {
        viewModelScope.launch {
            device.collect {
                _connected.value = it
            }
        }
    }
    fun onServiceUnbound() {
        _connected.value = null
    }
}