package com.example.driveselect.ui.modulos.alquiler


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.Auto
import com.example.driveselect.funciones.Calculos
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RentaViewModel : ViewModel() {

    fun procesarRentaInmediata(
        auto: Auto,
        nombreCliente: String,
        telefonoCliente: String,
        duiCliente: String,
        licenciaCliente: String,
        correoCliente: String,
        fechaInicio: Long,
        fechaFin: Long,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (auto.id.isBlank()) {
            android.util.Log.e("RENTA_DEBUG", "Error: El ID del auto está vacío")
            onError("ID de vehículo no válido")
            return
        }

        viewModelScope.launch {
            try {
                val dias = Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin)
                val costo = Calculos.calcularCostoTotal(dias, auto.precioPorDia)

                val nuevoAlquiler = Alquiler(
                    usuarioId = "PRESENCIAL",
                    autoId = auto.id,
                    autoMarca = auto.marca,
                    autoModelo = auto.modelo,
                    nombreCliente = nombreCliente.trim(),
                    telefonoCliente = telefonoCliente.trim(),
                    correoCliente = correoCliente.trim(),
                    documentoCliente = duiCliente.trim(),
                    licenciaCliente = licenciaCliente.trim(),
                    fechaRecogida = fechaInicio,
                    fechaEntrega = fechaFin,
                    fechaRecogidaTexto = Calculos.formatearFecha(fechaInicio),
                    fechaEntregaTexto = Calculos.formatearFecha(fechaFin),
                    diasTotales = dias,
                    costoTotal = costo,
                    estado = "en_uso" // Estado directo en uso
                )

                // 1. Guardar en alquileres
                val docRef = FirebaseService.db.collection("alquileres").add(nuevoAlquiler).await()
                docRef.update("id", docRef.id).await()
                android.util.Log.d("RENTA_DEBUG", "Alquiler guardado con ID: ${docRef.id}")

                // 2. Actualizar auto a ALQUILADO EN USO
                FirebaseService.db.collection("autos")
                    .document(auto.id)
                    .update("estado", "ALQUILADO EN USO")
                    .await()
                android.util.Log.d("RENTA_DEBUG", "Auto ${auto.id} actualizado a ALQUILADO EN USO")

                onExito()
            } catch (e: Exception) {
                android.util.Log.e("RENTA_DEBUG", "Error en renta: ${e.message}", e)
                onError(e.message ?: "Error al procesar la renta")
            }
        }
    }
}