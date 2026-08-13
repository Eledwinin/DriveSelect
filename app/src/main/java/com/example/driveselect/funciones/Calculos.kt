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

    // comprueba si un dia cae en los rangos de reserva ocupados
    fun esFechaOcupada(utcTimeMillis: Long, rangosOcupados: List<Pair<Long, Long>>): Boolean {
        // 1. Obtener la medianoche de HOY local
        val calHoy = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val hoyMs = calHoy.timeInMillis

        // 2. Convertir la fecha que el DatePicker está evaluando
        val offset = java.util.TimeZone.getDefault().getOffset(utcTimeMillis)
        val fechaEvalLocal = utcTimeMillis - offset
        val calEval = java.util.Calendar.getInstance().apply {
            timeInMillis = fechaEvalLocal
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val diaEvalMs = calEval.timeInMillis

        // BLOQUEO 1: Bloquear cualquier día anterior a hoy (1 al 12 de agosto)
        if (diaEvalMs < hoyMs) {
            return true
        }

        // BLOQUEO 2: Bloquear cualquier rango ocupado (14 al 20 de agosto)
        return rangosOcupados.any { (inicioMs, finMs) ->
            val calInicio = java.util.Calendar.getInstance().apply {
                timeInMillis = inicioMs
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val calFin = java.util.Calendar.getInstance().apply {
                timeInMillis = finMs
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }

            diaEvalMs in calInicio.timeInMillis..calFin.timeInMillis
        }
    }

    // Calcula el estado real del auto según la fecha actual
    fun obtenerEstadoRealAuto(
        estadoEnFirestore: String,
        fechaInicioReserva: Long? = null
    ): String {
        val estadoUpper = estadoEnFirestore.uppercase().trim()

        // Si el admin ya entregó el auto físicamente (Rojo), se respeta
        if (estadoUpper == "ALQUILADO EN USO" || estadoUpper == "EN USO") {
            return "ALQUILADO EN USO"
        }

        // Si no hay fecha o la reserva no es para hoy, HOY está disponible
        if (fechaInicioReserva == null) return "DISPONIBLE"

        val calHoy = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val hoyMs = calHoy.timeInMillis

        val calInicio = java.util.Calendar.getInstance().apply {
            timeInMillis = fechaInicioReserva
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val inicioMs = calInicio.timeInMillis

        // SOLO si la reserva inicia HOY pasa a Naranja ("ALQUILADO EN PROCESO")
        return if (inicioMs <= hoyMs) {
            "ALQUILADO EN PROCESO"
        } else {
            "DISPONIBLE"
        }
    }
}