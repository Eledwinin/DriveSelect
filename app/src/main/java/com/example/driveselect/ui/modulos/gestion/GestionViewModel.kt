package com.example.driveselect.ui.modulos.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.AutoEstado
import com.example.driveselect.data.repository.AutoRepository
import com.google.firebase.firestore.ListenerRegistration
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

    private val _alquileresActivos = MutableStateFlow<List<Alquiler>>(emptyList())
    val alquileresActivos: StateFlow<List<Alquiler>> = _alquileresActivos.asStateFlow()

    private var listenerAlquileres: ListenerRegistration? = null

    init {
        escucharAlquileresEnTiempoReal()
    }

    private fun escucharAlquileresEnTiempoReal() {
        listenerAlquileres?.remove()

        listenerAlquileres = FirebaseService.db.collection("alquileres")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val todos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Alquiler::class.java)?.copy(id = doc.id)
                }

                // solicitudes pendientes
                _solicitudesPendientes.value = todos.filter {
                    it.estado.lowercase().trim() in listOf("pendiente", "aprobado", "en proceso", "en_proceso")
                }

                //alquileres activos
                _alquileresActivos.value = todos.filter {
                    it.estado.lowercase().trim() in listOf("en_uso", "en uso", "alquilado_en_uso", "alquilado en uso")
                }
            }
    }

    // APROBAR / ENTREGAR VEHÍCULO AL CLIENTE
    fun aprobarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val batch = FirebaseService.db.batch()

                // marca el estado del alquiler a "en_uso"
                val docAlquiler = FirebaseService.db.collection("alquileres").document(alquiler.id)
                batch.update(docAlquiler, "estado", "en_uso")

                // cambia el auto a ALQUILADO EN USO (Rojo)
                val docAuto = FirebaseService.db.collection("autos").document(alquiler.autoId)
                batch.update(
                    docAuto, mapOf(
                        "estado" to AutoEstado.ALQUILADO_EN_USO.displayName,
                        "fechaInicio" to alquiler.fechaRecogida,
                        "fechaDevolucion" to alquiler.fechaEntrega
                    )
                )

                batch.commit().await()
                onExito()
            } catch (e: Exception) {
                FirebaseService.db.collection("alquileres").document(alquiler.id).update("estado", "en_uso")
                FirebaseService.db.collection("autos").document(alquiler.autoId).update("estado", AutoEstado.ALQUILADO_EN_USO.displayName)
                onExito()
            }
        }
    }

    // RECHAZAR / CANCELAR
    fun rechazarReserva(alquiler: Alquiler, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                FirebaseService.db.collection("alquileres").document(alquiler.id).update("estado", "cancelado").await()
                FirebaseService.db.collection("autos").document(alquiler.autoId).update("estado", AutoEstado.DISPONIBLE.displayName).await()
                onExito()
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    // RECIBIR / FINALIZAR
    fun finalizarAlquiler(alquiler: Alquiler, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                FirebaseService.db.collection("alquileres").document(alquiler.id).update("estado", "finalizado").await()
                FirebaseService.db.collection("autos").document(alquiler.autoId).update("estado", AutoEstado.DISPONIBLE.displayName).await()
                onExito()
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    fun obtenerSolicitudesOrdenadas(solicitudes: List<Alquiler>): List<Alquiler> {
        return solicitudes.sortedBy { it.fechaRecogida }
    }

    override fun onCleared() {
        super.onCleared()
        listenerAlquileres?.remove()
        listenerAlquileres = null
    }
}