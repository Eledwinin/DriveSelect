package com.example.driveselect.data.model

data class Auto(
    val id: String = "",
    val marca: String = "",
    val modelo: String = "",
    val precioPorDia: Double = 0.0,
    val estado: String = AutoEstado.DISPONIBLE.name,
    val imagenUrl: String = "",
    val motor: String = "" // campo que mostrare en ficha tecnica de solo lectura
)