package com.example.driveselect.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.ui.modulos.alquiler.AlquilerScreen
import com.example.driveselect.ui.modulos.alquiler.AlquilerViewModel
import com.example.driveselect.ui.modulos.alquiler.RentaScreen
import com.example.driveselect.ui.modulos.alquiler.RentaViewModel
import com.example.driveselect.ui.modulos.gestion.GestionSolicitudesScreen
import com.example.driveselect.ui.modulos.gestion.GestionViewModel
import com.example.driveselect.ui.modulos.historial.HistorialScreen
import com.example.driveselect.ui.modulos.inventario.AutoListScreen
import com.example.driveselect.ui.modulos.inventario.InventarioViewModel
import com.example.driveselect.ui.modulos.login.AuthViewModel
import com.example.driveselect.ui.modulos.login.LoginScreen
import com.example.driveselect.ui.modulos.login.LoginViewModel
import com.example.driveselect.ui.modulos.login.OlvideClaveScreen
import com.example.driveselect.ui.modulos.login.RegisterScreen
import com.example.driveselect.ui.modulos.perfil.PerfilScreen
import com.example.driveselect.ui.modulos.perfil.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val rolUsuario by authViewModel.rol.collectAsState()
    val esAdmin = (rolUsuario == "admin")

    val itemsBarra = if (esAdmin) {
        listOf(Rutas.Gestion, Rutas.Inventario, Rutas.Perfil)
    } else {
        listOf(Rutas.Inventario, Rutas.Historial, Rutas.Perfil)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (rutaActual in listOf(Rutas.Inventario.ruta, Rutas.Historial.ruta, Rutas.Gestion.ruta, Rutas.Perfil.ruta)) {
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
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
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
            startDestination = Rutas.Login.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // CATÁLOGO DE AUTOS
            composable(Rutas.Inventario.ruta) { backStackEntry ->
                val inventarioViewModel: InventarioViewModel = viewModel(backStackEntry)

                AutoListScreen(
                    viewModel = inventarioViewModel,
                    esAdmin = esAdmin,
                    onReservarClick = { auto ->
                        inventarioViewModel.seleccionarAuto(auto)
                        navController.navigate(Rutas.Alquiler.ruta)
                    },
                    onRentarClick = { auto ->
                        inventarioViewModel.seleccionarAuto(auto)
                        navController.navigate(Rutas.Renta.ruta)
                    }

                )
            }

            // LOGIN
            composable(Rutas.Login.ruta) {
                val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginExitoso = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            FirebaseService.db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    val rol = doc.getString("rol") ?: "cliente"
                                    authViewModel.obtenerRolUsuario()

                                    val destino = if (rol == "admin") Rutas.Gestion.ruta else Rutas.Inventario.ruta
                                    navController.navigate(destino) {
                                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    navController.navigate(Rutas.Inventario.ruta) {
                                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                                    }
                                }
                        }
                    },
                    onIrARegistro = {
                        navController.navigate(Rutas.Registro.ruta)
                    },
                    onOlvideClaveClick = {
                        navController.navigate(Rutas.OlvideClave.ruta)
                    }
                )
            }

            // REGISTRO
            composable(Rutas.Registro.ruta) {
                RegisterScreen(
                    onRegistroExitoso = {
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(Rutas.Registro.ruta) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // OLVIDÉ CONTRASEÑA
            composable(Rutas.OlvideClave.ruta) {
                OlvideClaveScreen(
                    onCorreoEnviadoExito = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // RESERVAR AUTO (CLIENTE Y ADMIN)
            composable(Rutas.Alquiler.ruta) {
                val inventarioEntry = remember(it) {
                    navController.getBackStackEntry(Rutas.Inventario.ruta)
                }
                val inventarioViewModel: InventarioViewModel = viewModel(inventarioEntry)
                val alquilerViewModel: AlquilerViewModel = viewModel()

                val autoSeleccionado by inventarioViewModel.autoSeleccionado.collectAsState()

                if (autoSeleccionado != null) {
                    AlquilerScreen(
                        auto = autoSeleccionado!!,
                        viewModel = alquilerViewModel,
                        esAdmin = esAdmin,
                        onReservaExitosa = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            // HISTORIAL
            composable(Rutas.Historial.ruta) {
                if (esAdmin) {
                    navController.navigate(Rutas.Gestion.ruta)
                } else {
                    HistorialScreen()
                }
            }

            // GESTIÓN DE SOLICITUDES
            composable(Rutas.Gestion.ruta) {
                if (!esAdmin) {
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

            // PERFIL
            composable(Rutas.Perfil.ruta) {
                val perfilViewModel: PerfilViewModel = viewModel()

                PerfilScreen(
                    viewModel = perfilViewModel,
                    onCerrarSesion = {
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            //para renta
            composable(Rutas.Renta.ruta) {
                val inventarioEntry = remember(it) {
                    navController.getBackStackEntry(Rutas.Inventario.ruta)
                }
                val inventarioViewModel: InventarioViewModel = viewModel(inventarioEntry)
                val rentaViewModel: RentaViewModel = viewModel()
                val autoSeleccionado by inventarioViewModel.autoSeleccionado.collectAsState()

                if (autoSeleccionado != null) {
                    RentaScreen(
                        auto = autoSeleccionado!!,
                        viewModel = rentaViewModel,
                        onRentaExitosa = {
                            navController.popBackStack()
                        },
                        onVolver = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
        }
    }
}