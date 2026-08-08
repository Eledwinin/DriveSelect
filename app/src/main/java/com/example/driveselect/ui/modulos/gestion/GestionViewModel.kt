package com.example.driveselect.ui.modulos.gestion

import androidx.lifecycle.ViewModel
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.AutoEstado
import com.example.driveselect.data.repository.AutoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GestionViewModel(
    private val repository: AutoRepository = AutoRepository()
) : ViewModel() {

    private val _solicitudesPendientes = MutableStateFlow<List<Alquiler>>(emptyList())
    val solicitudesPendientes: StateFlow<List<Alquiler>> = _solicitudesPendientes.asStateFlow()
    // Lista de alquileres activos
    private val _alquileresActivos = MutableStateFlow<List<Alquiler>>(emptyList())
    val alquileresActivos: StateFlow<List<Alquiler>> = _alquileresActivos.asStateFlow()

    init {
        cargarSolicitudesPendientes()
        cargarAlquileresActivos()
    }

    fun cargarSolicitudesPendientes() {
        repository.obtenerAlquileresPorEstado("pendiente") { lista ->
            _solicitudesPendientes.value = lista
        }
    }
    fun cargarAlquileresActivos() {
        repository.obtenerAlquileresPorEstado("en uso") { lista ->
            _alquileresActivos.value = lista
        }
    }



    // APROBAR / ENTREGAR
    fun aprobarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        repository.actualizarEstadoAlquiler(alquiler.id, "en uso") {
            repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.ALQUILADO_EN_USO) {
                onExito()
            }
        }
    }

    // RECHAZAR / CANCELAR
    fun rechazarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        repository.actualizarEstadoAlquiler(alquiler.id, "cancelado") {
            repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.DISPONIBLE) {
                onExito()
            }
        }
    }

    // RECIBIR / FINALIZAR
    fun finalizarAlquiler(alquiler: Alquiler, onExito: () -> Unit = {}) {
        repository.actualizarEstadoAlquiler(alquiler.id, "finalizado") {
            repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.DISPONIBLE) {
                onExito()
            }
        }
    }
}