package com.example.driveselect.ui.modulos.inventario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.model.AutoEstado
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
    private var listenerSolicitudes: ListenerRegistration? = null

    // Cache interno de autos para recalcular con las solicitudes
    private var listaAutosRaw: List<Auto> = emptyList()

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
        listenerSolicitudes?.remove()

        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.iniciarInventario {
                    // Escuchar colección de autos
                    listenerAutos = repository.obtenerAutos { lista ->
                        listaAutosRaw = lista

                        // Escuchar alquileres para sincronizar el estado hoy
                        if (listenerSolicitudes == null) {
                            listenerSolicitudes = FirebaseService.db.collection("alquileres")
                                .addSnapshotListener { alqSnap, _ ->
                                    val calHoy = java.util.Calendar.getInstance().apply {
                                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                                        set(java.util.Calendar.MINUTE, 59)
                                        set(java.util.Calendar.SECOND, 59)
                                        set(java.util.Calendar.MILLISECOND, 999)
                                    }
                                    val finDeHoyMs = calHoy.timeInMillis

                                    // IDs de autos que tienen reserva para hoy
                                    val autosEnProcesoHoy = mutableSetOf<String>()
                                    val autosEnUso = mutableSetOf<String>()

                                    if (alqSnap != null) {
                                        for (doc in alqSnap.documents) {
                                            val autoId = doc.getString("autoId") ?: ""
                                            val estado = (doc.getString("estado") ?: "").lowercase().trim()
                                            val fechaRecogida = doc.getLong("fechaRecogida") ?: doc.getLong("fechaInicio") ?: 0L

                                            if (autoId.isNotBlank()) {
                                                if (estado in listOf("en_uso", "en uso", "alquilado_en_uso", "alquilado en uso")) {
                                                    autosEnUso.add(autoId)
                                                } else if (estado in listOf("pendiente", "aprobado", "en proceso", "en_proceso")) {
                                                    if (fechaRecogida > 0L && fechaRecogida <= finDeHoyMs) {
                                                        autosEnProcesoHoy.add(autoId)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Asignar el estado visual directamente
                                    val listaActualizada = listaAutosRaw.map { auto ->
                                        val estadoActual = auto.estado.uppercase().trim()
                                        if (auto.id in autosEnUso || estadoActual in listOf("ALQUILADO EN USO", "EN USO", "ALQUILADO_EN_USO")) {
                                            auto.copy(estado = AutoEstado.ALQUILADO_EN_USO.displayName)
                                        } else if (auto.id in autosEnProcesoHoy) {
                                            auto.copy(estado = AutoEstado.ALQUILADO_EN_PROCESO.displayName)
                                        } else {
                                            auto.copy(estado = AutoEstado.DISPONIBLE.displayName)
                                        }
                                    }

                                    _autos.value = listaActualizada
                                    _isLoading.value = false
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun procesarEstadosAutos(solicitudes: List<Alquiler>) {
        val calHoy = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val hoyMs = calHoy.timeInMillis

        val listaFinal = listaAutosRaw.map { auto ->
            val estadoActual = auto.estado.uppercase().trim()

            // Si el admin ya entregó físicamente el vehículo (Rojo)
            if (estadoActual in listOf("ALQUILADO EN USO", "EN USO", "ALQUILADO_EN_USO")) {
                auto.copy(estado = AutoEstado.ALQUILADO_EN_USO.displayName)
            } else {
                // Verificar si tiene alguna solicitud activa cuya fecha sea HOY
                val tieneSolicitudHoy = solicitudes.any { sol ->
                    if (sol.autoId != auto.id) return@any false

                    val estadoSol = sol.estado.lowercase().trim()
                    if (estadoSol in listOf("cancelado", "rechazado", "finalizado")) return@any false

                    val calInicio = java.util.Calendar.getInstance().apply {
                        timeInMillis = sol.fechaRecogida
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }

                    // Si la reserva inicia HOY o ya pasó la fecha de recogida
                    calInicio.timeInMillis <= hoyMs
                }

                if (tieneSolicitudHoy) {
                    auto.copy(estado = AutoEstado.ALQUILADO_EN_PROCESO.displayName)
                } else {
                    auto.copy(estado = AutoEstado.DISPONIBLE.displayName)
                }
            }
        }

        _autos.value = listaFinal
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        listenerAutos?.remove()
        listenerSolicitudes?.remove()
        listenerAutos = null
        listenerSolicitudes = null
    }

    // carga las reservas activas en Firestore para saber qué autos están ocupados en esas fechas
    fun filtrarPorRangoFechas(inicioMillis: Long, finMillis: Long) {
        fechaInicioFiltro = inicioMillis
        fechaFinFiltro = finMillis

        viewModelScope.launch {
            try {
                val calInicioFiltro = java.util.Calendar.getInstance().apply {
                    timeInMillis = inicioMillis
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val inicioFiltroMs = calInicioFiltro.timeInMillis

                val calFinFiltro = java.util.Calendar.getInstance().apply {
                    timeInMillis = finMillis
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }
                val finFiltroMs = calFinFiltro.timeInMillis

                val snapshot = FirebaseService.db.collection("alquileres").get().await()
                val ocupados = mutableSetOf<String>()

                for (doc in snapshot.documents) {
                    val autoId = doc.getString("autoId") ?: continue
                    val resInicio = doc.getLong("fechaRecogida") ?: doc.getLong("fechaInicio") ?: continue
                    val resFin = doc.getLong("fechaEntrega") ?: doc.getLong("fechaFin") ?: continue
                    val estado = (doc.getString("estado") ?: "").lowercase().trim()

                    // Omitir alquileres cancelados o rechazados
                    if (estado in listOf("cancelado", "rechazado")) {
                        continue
                    }

                    // verifica si el alquiler está dentro del rango de fechas
                    if (inicioFiltroMs <= resFin && finFiltroMs >= resInicio) {
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