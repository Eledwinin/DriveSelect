package com.example.driveselect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driveselect.ui.modulos.alquiler.AlquilerViewModel
import com.example.driveselect.ui.modulos.gestion.GestionViewModel
import com.example.driveselect.ui.modulos.inventario.AutoListScreen
import com.example.driveselect.ui.modulos.inventario.InventarioViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Inventario.ruta
    ) {
        // pantalla principa.
        composable(Rutas.Inventario.ruta) {
            val inventarioViewModel: InventarioViewModel = viewModel()
            val gestionViewModel: GestionViewModel = viewModel()

            AutoListScreen(
                viewModel = inventarioViewModel,
                onReservarClick = { auto ->
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

        // formulario para reservar
        composable(Rutas.Alquiler.ruta) {
            val alquilerViewModel: AlquilerViewModel = viewModel()

        }

        // formulario para gestionar
        composable(Rutas.Gestion.ruta) {
            val gestionViewModel: GestionViewModel = viewModel()

        }
    }
}