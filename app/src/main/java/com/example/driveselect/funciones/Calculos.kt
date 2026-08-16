package com.example.driveselect.funciones

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Calculos {

    //tarifa de mora por dia tardado en entregar carro
    const val TARIFA_MORA_POR_DIA = 20.00

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

    // Comprueba si un día cae en los rangos de reserva ocupados o es del pasado
    fun esFechaOcupada(utcTimeMillis: Long, rangosOcupados: List<Pair<Long, Long>>): Boolean {
        // 1. Medianoche de HOY en la zona horaria del sistema convertida a base UTC del día
        val calHoy = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val hoyLocalMs = calHoy.timeInMillis

        // Normalizar la fecha que entrega el DatePicker
        val calEval = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcTimeMillis
        }

        // Creamos la misma fecha en calendario local para comparar día exacto
        val calEvalLocal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, calEval.get(java.util.Calendar.YEAR))
            set(java.util.Calendar.MONTH, calEval.get(java.util.Calendar.MONTH))
            set(java.util.Calendar.DAY_OF_MONTH, calEval.get(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val diaEvalMs = calEvalLocal.timeInMillis

        //bloquea SOLO días anteriores a hoy
        if (diaEvalMs < hoyLocalMs) {
            return true
        }

        // bloquea los rangos donde el auto ya tiene una reserva activa
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


    // calcula cuantos dias de retraso lleva
    fun calcularDiasMora(fechaEntregaMs: Long): Int {
        val calHoy = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val calEntrega = java.util.Calendar.getInstance().apply {
            timeInMillis = fechaEntregaMs
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val diffMs = calHoy.timeInMillis - calEntrega.timeInMillis
        return if (diffMs > 0) {
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
    }

    // calcula el monto total de mora
    fun calcularMontoMora(diasMora: Int, tarifaPorDia: Double = TARIFA_MORA_POR_DIA): Double {
        return diasMora * tarifaPorDia
    }
}