package it.urronio.mirror.presentation.viewmodel

import androidx.lifecycle.ViewModel
import it.urronio.mirror.data.model.Radio
import it.urronio.mirror.data.repository.RadioRepository
import it.urronio.mirror.data.repository.SerialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RadioViewModel(
    private val repository: RadioRepository,
    private val serial: SerialRepository,
    private val name: String
): ViewModel() {
    private val _radio: MutableStateFlow<Radio?> = MutableStateFlow(value = null)
    val radio: StateFlow<Radio?> = _radio
    init {
        _radio.value = repository.getRadioByName(name = name)
    }
    fun requestPermission() {
        if (_radio.value != null)
            serial.requestPermission(_radio.value!!.device)
    }
}