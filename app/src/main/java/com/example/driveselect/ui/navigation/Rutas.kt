package com.example.driveselect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Rutas(val ruta: String, val titulo: String, val icono: ImageVector) {
    object Inventario : Rutas("inventario", "Catálogo", Icons.Default.DirectionsCar)
    object Alquiler : Rutas("alquiler", "Reservar", Icons.Default.DirectionsCar)
    object Gestion : Rutas("gestion", "Solicitudes", Icons.Default.ListAlt)
    object Historial : Rutas("historial", "Historial", Icons.Default.History)
    object Login : Rutas("login", "Login", Icons.Default.Login)
    object Registro : Rutas("registro", "Registro", Icons.Default.PersonAdd)
    object OlvideClave : Rutas("olvideClave", "Olvide mi clave", Icons.Default.PersonAdd)
}