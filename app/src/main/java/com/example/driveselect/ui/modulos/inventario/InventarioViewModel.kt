package com.example.driveselect.ui.modulos.inventario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.repository.AutoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    var fechaInicioFiltro by mutableStateOf<Long?>(null)
    var fechaFinFiltro by mutableStateOf<Long?>(null)

    // ESTADO OBSERVABLE PARA RECOMPONER LA VISTA AL CAMBIAR LOS OCUPADOS
    var idsAutosOcupados by mutableStateOf<Set<String>>(emptySet())
        private set

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

    // carga las reservas activas en Firestore para saber qué autos están ocupados en esas fechas
    fun filtrarPorRangoFechas(inicioMillis: Long, finMillis: Long) {
        fechaInicioFiltro = inicioMillis
        fechaFinFiltro = finMillis

        viewModelScope.launch {
            try {

                val snapshot = FirebaseService.db.collection("solicitudes")
                    .get()
                    .await()

                val ocupados = mutableSetOf<String>()

                for (doc in snapshot.documents) {
                    val autoId = doc.getString("autoId") ?: continue
                    val resInicio = doc.getLong("fechaInicio") ?: continue
                    val resFin = doc.getLong("fechaFin") ?: continue
                    val estado = (doc.getString("estado") ?: "").uppercase()

                    // Ignoramos solicitudes rechazadas o canceladas
                    if (estado.contains("RECHAZADO") || estado.contains("CANCELADO")) {
                        continue
                    }


                    // Si la fecha solicitada se cruza con la reserva existente
                    if (inicioMillis <= resFin && finMillis >= resInicio) {
                        ocupados.add(autoId)
                    }
                }

                idsAutosOcupados = ocupados
            } catch (e: Exception) {
                idsAutosOcupados = emptySet()
            }
        }
    }

    fun limpiarFiltroFechas() {
        fechaInicioFiltro = null
        fechaFinFiltro = null
        idsAutosOcupados = emptySet()
    }

    fun autoEstaDisponibleEnRango(autoId: String): Boolean {
        if (fechaInicioFiltro == null || fechaFinFiltro == null) return true
        return autoId !in idsAutosOcupados
    }
}