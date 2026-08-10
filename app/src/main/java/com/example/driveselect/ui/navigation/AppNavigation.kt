package com.example.driveselect.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.driveselect.data.model.Auto
import com.example.driveselect.ui.modulos.alquiler.AlquilerScreen
import com.example.driveselect.ui.modulos.alquiler.AlquilerViewModel
import com.example.driveselect.ui.modulos.gestion.GestionSolicitudesScreen
import com.example.driveselect.ui.modulos.gestion.GestionViewModel
import com.example.driveselect.ui.modulos.historial.HistorialScreen
import com.example.driveselect.ui.modulos.inventario.AutoListScreen
import com.example.driveselect.ui.modulos.inventario.InventarioViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val rolUsuario by authViewModel.rol.collectAsState()
    val inventarioViewModel: InventarioViewModel = viewModel()

    // Pestañas según el rol
    val itemsBarra = if (rolUsuario == "admin") {
        listOf(Rutas.Gestion, Rutas.Inventario)
    } else {
        listOf(Rutas.Inventario, Rutas.Historial)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (rutaActual in listOf(Rutas.Inventario.ruta, Rutas.Historial.ruta, Rutas.Gestion.ruta)) {
                NavigationBar(
                    containerColor = Color(0xFF13141C),
                    contentColor = Color.White
                ) {
                    itemsBarra.forEach { item ->
                        val selected = rutaActual == item.ruta
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = {
                                Text(
                                    text = item.titulo,
                                    color = if (selected) Color(0xFFFF9800) else Color(0xFF8E8E93)
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icono,
                                    contentDescription = item.titulo,
                                    tint = if (selected) Color(0xFFFF9800) else Color(0xFF8E8E93)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (rolUsuario == "admin") Rutas.Gestion.ruta else Rutas.Inventario.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // CATÁLOGO DE AUTOS
            composable(Rutas.Inventario.ruta) {
                AutoListScreen(
                    viewModel = inventarioViewModel,
                    esAdmin = (rolUsuario == "admin"),
                    onReservarClick = { auto ->
                        if (rolUsuario != "admin") {
                            inventarioViewModel.seleccionarAuto(auto)
                            navController.navigate(Rutas.Alquiler.ruta)
                        }
                    },
                    onGestionClick = {
                        if (rolUsuario == "admin") {
                            navController.navigate(Rutas.Gestion.ruta)
                        }
                    }
                )
            }

            // RESERVAR AUTO
            composable(Rutas.Alquiler.ruta) {
                if (rolUsuario == "admin") {
                    navController.navigate(Rutas.Gestion.ruta)
                } else {
                    val alquilerViewModel: AlquilerViewModel = viewModel()
                    val autoSeleccionado by inventarioViewModel.autoSeleccionado.collectAsState()

                    autoSeleccionado?.let { auto: Auto ->
                        AlquilerScreen(
                            auto = auto,
                            viewModel = alquilerViewModel,
                            onReservaExitosa = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            // HISTORIAL
            composable(Rutas.Historial.ruta) {
                if (rolUsuario == "admin") {
                    navController.navigate(Rutas.Gestion.ruta)
                } else {
                    HistorialScreen()
                }
            }

            // GESTIÓN DE SOLICITUDES
            composable(Rutas.Gestion.ruta) {
                if (rolUsuario != "admin") {
                    navController.navigate(Rutas.Inventario.ruta)
                } else {
                    val gestionViewModel: GestionViewModel = viewModel()
                    GestionSolicitudesScreen(
                        viewModel = gestionViewModel,
                        onVolverClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}