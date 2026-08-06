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
import kotlin.collections.emptyList

class InventarioViewModel(
    private val repository: AutoRepository = AutoRepository()
): ViewModel()  {
    private val _autos = MutableStateFlow<List<Auto>>(emptyList())
    val autos: StateFlow<List<Auto>> = _autos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var ListenerAutos: ListenerRegistration? = null

    init {
        cargarInventario()
    }

    private fun cargarInventario() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.iniciarInventario {
                ListenerAutos = repository.obtenerAutos { lista ->
                    _autos.value = lista
                    _isLoading.value = false
                }
            }
        }
    }

    override fun onCleared(){
        super.onCleared()
        ListenerAutos?.remove()
    }

}