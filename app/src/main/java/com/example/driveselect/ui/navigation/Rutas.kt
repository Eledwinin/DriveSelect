package com.example.driveselect.ui.navigation

sealed class Rutas(val ruta: String){
    object Inventario : Rutas("inventario")
    object Alquiler : Rutas("alquiler")
    object Gestion : Rutas("gestion")
    object Login : Rutas("login")
    object Registro : Rutas("registro")
}