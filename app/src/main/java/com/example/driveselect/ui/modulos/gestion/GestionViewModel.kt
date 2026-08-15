package com.example.driveselect.ui.modulos.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.AutoEstado
import com.example.driveselect.data.repository.AutoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    // APROBAR / ENTREGAR VEHÍCULO AL CLIENTE
    fun aprobarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val batch = FirebaseService.db.batch()

                // 1. Marcar el estado de la solicitud/alquiler a "en uso"
                val docSolicitud = FirebaseService.db.collection("solicitudes").document(alquiler.id)
                batch.update(docSolicitud, "estado", "en uso")

                // 2. Cambiar el auto a ALQUILADO EN USO (Rojo) y guardar fechas activas
                val docAuto = FirebaseService.db.collection("autos").document(alquiler.autoId)
                batch.update(
                    docAuto, mapOf(
                        "estado" to AutoEstado.ALQUILADO_EN_USO.displayName,
                        "fechaInicio" to alquiler.fechaRecogida,
                        "fechaDevolucion" to alquiler.fechaEntrega
                    )
                )

                // Ejecutar ambas operaciones en batch
                batch.commit().await()

                // Recargar listas locales
                cargarSolicitudesPendientes()
                cargarAlquileresActivos()
                onExito()
            } catch (e: Exception) {
                // Manejo de error o reintento tradicional
                repository.actualizarEstadoAlquiler(alquiler.id, "en uso") {
                    repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.ALQUILADO_EN_USO) {
                        cargarSolicitudesPendientes()
                        cargarAlquileresActivos()
                        onExito()
                    }
                }
            }
        }
    }

    // RECHAZAR / CANCELAR
    fun rechazarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        repository.actualizarEstadoAlquiler(alquiler.id, "cancelado") {
            repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.DISPONIBLE) {
                cargarSolicitudesPendientes()
                cargarAlquileresActivos()
                onExito()
            }
        }
    }

    // RECIBIR / FINALIZAR
    fun finalizarAlquiler(alquiler: Alquiler, onExito: () -> Unit = {}) {
        repository.actualizarEstadoAlquiler(alquiler.id, "finalizado") {
            repository.actualizarEstadoAuto(alquiler.autoId, AutoEstado.DISPONIBLE) {
                cargarSolicitudesPendientes()
                cargarAlquileresActivos()
                onExito()
            }
        }
    }


    fun obtenerSolicitudesOrdenadas(solicitudes: List<Alquiler>): List<Alquiler> {
        return solicitudes.sortedBy { it.fechaRecogida }
    }
}