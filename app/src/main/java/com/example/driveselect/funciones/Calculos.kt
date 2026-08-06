package com.example.driveselect.funciones

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Calculos {

    // esto va a calcular cuantos días hay entre dos fechas en milisegundos
    fun calcularDiasDeAlquiler(fechaInicio: Long, fechaFin: Long): Int {
        if (fechaFin <= fechaInicio) return 1
        val diferencia = fechaFin - fechaInicio
        val dias = (diferencia / (1000 * 60 * 60 * 24)).toInt()
        return if (dias == 0) 1 else dias
    }

    // esto multiplica los dias por el costo del vehiculo
    fun calcularCostoTotal(dias: Int, precioPorDia: Double): Double {
        return dias * precioPorDia
    }

    // formatea a moneda
    fun formatearMoneda(monto: Double): String {
        return String.format(Locale.US, "$%.2f", monto)
    }

    // convierte un timestamp en milisegundos a un texto legible ("05/08/2026")
    fun formatearFecha(timestampMs: Long): String {
        if (timestampMs == 0L) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }
}