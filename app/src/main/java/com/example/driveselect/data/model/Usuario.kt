package com.example.driveselect.data.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val dui: String = "",
    val licencia: String = "",
    val rol: String = "cliente"
)