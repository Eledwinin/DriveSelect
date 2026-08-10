package com.example.driveselect.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Alquiler(
    val id: String = "",
    val usuarioId: String = "",
    val autoId: String = "",
    val autoMarca: String = "",
    val autoModelo: String = "",
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val correoCliente: String = "",
    val documentoCliente: String = "",
    val licenciaCliente: String = "",
    val fechaRecogida: Long = 0L,
    val fechaEntrega: Long = 0L,
    val fechaRecogidaTexto: String = "",
    val fechaEntregaTexto: String = "",
    val diasTotales: Int = 0,
    val costoTotal: Double = 0.0,
    val estado: String = "pendiente"
)