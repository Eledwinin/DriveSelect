package com.example.driveselect.ui.modulos.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.repository.AutoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val repository: AutoRepository = AutoRepository()
) : ViewModel() {

    private val _autos = MutableStateFlow<List<Auto>>(emptyList())
    val autos: StateFlow<List<Auto>> = _autos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _autoSeleccionado = MutableStateFlow<Auto?>(null)
    val autoSeleccionado: StateFlow<Auto?> = _autoSeleccionado.asStateFlow()

    private var listenerAutos: ListenerRegistration? = null

    init {
        cargarInventario()
    }

    fun seleccionarAuto(auto: Auto) {
        _autoSeleccionado.value = auto
    }

    private fun cargarInventario() {

        listenerAutos?.remove()

        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.iniciarInventario {
                    listenerAutos = repository.obtenerAutos { lista ->
                        _autos.value = lista
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerAutos?.remove()
        listenerAutos = null
    }
}