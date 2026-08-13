package com.example.driveselect.ui.modulos.alquiler

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
import com.example.driveselect.funciones.Calculos
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AlquilerViewModel(
    private val repository: AutoRepository = AutoRepository()
): ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var telefonoInput by mutableStateOf("")
    var duiInput by mutableStateOf("")

    var mostrarDialogoDatos by mutableStateOf(false)
    var isGuardandoDatos by mutableStateOf(false)
    var errorDatos by mutableStateOf<String?>(null)

    fun procesarReserva(
        auto: Auto,
        usuarioId: String,
        nombreCliente: String,
        telefonoCliente: String,
        duiCliente: String,
        licenciaCliente: String,
        correoCliente: String,
        fechaInicio: Long,
        fechaFin: Long,
        onExito: () -> Unit
    ){
        val dias = Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin)
        val costo = Calculos.calcularCostoTotal(dias, auto.precioPorDia)

        val nuevoAlquiler = Alquiler(
            usuarioId = usuarioId,
            autoId = auto.id,
            autoMarca = auto.marca,
            autoModelo = auto.modelo,
            nombreCliente = nombreCliente,
            telefonoCliente = telefonoCliente,
            correoCliente = correoCliente,
            documentoCliente = duiCliente,
            licenciaCliente = licenciaCliente,
            fechaRecogida = fechaInicio,
            fechaEntrega = fechaFin,
            fechaRecogidaTexto = Calculos.formatearFecha(fechaInicio),
            fechaEntregaTexto = Calculos.formatearFecha(fechaFin),
            diasTotales = dias,
            costoTotal = costo,
            estado = "pendiente"
        )

        repository.registrarAlquiler(nuevoAlquiler) {
            //verifica si la fecha de inicio es HOY
            val hoyUtc = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L)
            val inicioUtc = fechaInicio - (fechaInicio % 86400000L)
            val esParaHoy = inicioUtc <= hoyUtc

            if (esParaHoy) {
                // Solo si la reserva empieza hoy cambia el estado en el catálogo a ALQUILADO_EN_PROCESO
                repository.actualizarEstadoAuto(auto.id, AutoEstado.ALQUILADO_EN_PROCESO) {
                    onExito()
                }
            } else {
                // Si es para una fecha futura, el auto se mantiene en DISPONIBLE hoy
                onExito()
            }
        }
    }

    // verifica si al usuario le faltan datos en Firestore
    fun verificarDatosYProcesar(onListoParaReservar: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val doc = FirebaseService.db.collection("usuarios").document(uid).get().await()
                val telefono = doc.getString("telefono") ?: ""
                val dui = doc.getString("dui") ?: ""

                if (telefono.isBlank() || dui.isBlank()) {
                    telefonoInput = telefono
                    duiInput = dui
                    mostrarDialogoDatos = true
                } else {
                    onListoParaReservar()
                }
            } catch (e: Exception) {

            }
        }
    }

    // Guarda DUI y Teléfono en Firestore
    fun guardarDatosCliente(onExito: () -> Unit) {
        if (telefonoInput.isBlank() || duiInput.isBlank()) {
            errorDatos = "Por favor completa ambos campos"
            return
        }

        val uid = auth.currentUser?.uid ?: return
        isGuardandoDatos = true
        errorDatos = null

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "telefono" to telefonoInput.trim(),
                    "dui" to duiInput.trim()
                )
                FirebaseService.db.collection("usuarios").document(uid).update(updates).await()

                isGuardandoDatos = false
                mostrarDialogoDatos = false
                onExito()
            } catch (e: Exception) {
                isGuardandoDatos = false
                errorDatos = e.localizedMessage ?: "Error al guardar información"
            }
        }
    }

}