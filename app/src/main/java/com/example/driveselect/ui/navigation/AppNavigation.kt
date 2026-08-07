package com.example.driveselect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driveselect.data.model.Auto
import com.example.driveselect.ui.modulos.alquiler.AlquilerScreen
import com.example.driveselect.ui.modulos.alquiler.AlquilerViewModel
import com.example.driveselect.ui.modulos.gestion.GestionViewModel
import com.example.driveselect.ui.modulos.inventario.AutoListScreen
import com.example.driveselect.ui.modulos.inventario.InventarioViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val inventarioViewModel: InventarioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Rutas.Inventario.ruta
    ) {
        // PANTALLA PRINCIPAL
        composable(Rutas.Inventario.ruta) {
            val gestionViewModel: GestionViewModel = viewModel()

            AutoListScreen(
                viewModel = inventarioViewModel,
                onReservarClick = { auto ->
                    inventarioViewModel.seleccionarAuto(auto)
                    navController.navigate(Rutas.Alquiler.ruta)
                },
                onEntregarClick = { auto ->
                    gestionViewModel.entregarVehiculo(auto.id)
                },
                onDevolverClick = { auto ->
                    gestionViewModel.devolverVehiculo(auto.id)
                }
            )
        }

        // FORMULARIO PARA RESERVAR
        composable(Rutas.Alquiler.ruta) {
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

        // FORMULARIO PARA GESTIONAR
        composable(Rutas.Gestion.ruta) {
            val gestionViewModel: GestionViewModel = viewModel()
        }
    }
}