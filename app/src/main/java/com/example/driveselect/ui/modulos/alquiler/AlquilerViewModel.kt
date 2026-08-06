package com.example.driveselect.ui.modulos.alquiler

import androidx.lifecycle.ViewModel
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.repository.AutoRepository
import com.example.driveselect.funciones.Calculos

class AlquilerViewModel(
    private val repository: AutoRepository = AutoRepository()
): ViewModel() {
    fun procesarReserva(
        auto: Auto,
        nombreCliente: String,
        fechaInicio: Long,
        fechaFin: Long,
        onExito: () -> Unit
    ){
        val dias = Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin)
        val costo = Calculos.calcularCostoTotal(dias, auto.precioPorDia)
        val nuevoAlquiler = Alquiler(
            autoId = auto.id,
            autoMarca = auto.marca,
            autoModelo = auto.modelo,
            nombreCliente = nombreCliente,
            fechaRecogida = fechaFin,
            fechaEntrega = fechaInicio,
            fechaRecogidaTexto = Calculos.formatearFecha(fechaInicio),
            fechaEntregaTexto = Calculos.formatearFecha(fechaFin),
            diasTotales = dias,
            costoTotal = costo
        )
        repository.registrarAlquiler(nuevoAlquiler){
            onExito()
        }
    }
}