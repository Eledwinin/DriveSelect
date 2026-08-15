package com.example.driveselect.ui.modulos.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*

@Composable
fun GestionSolicitudesScreen(
    viewModel: GestionViewModel,
    onVolverClick: () -> Unit = {}
) {
    val solicitudes by viewModel.solicitudesPendientes.collectAsState()
    val activos by viewModel.alquileresActivos.collectAsState()
    var tabSeleccionada by remember { mutableStateOf(0) }

    // Límite de fin del día actual (23:59:59) para segmentar
    val calHoy = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
    }
    val finHoyMs = calHoy.timeInMillis

    // Separación y ordenamiento por fecha de recogida
    val solicitudesOrdenadas = remember(solicitudes) {
        solicitudes.sortedBy { it.fechaRecogida }
    }
    val entregasHoy = remember(solicitudesOrdenadas) {
        solicitudesOrdenadas.filter { it.fechaRecogida <= finHoyMs }
    }
    val proximasEntregas = remember(solicitudesOrdenadas) {
        solicitudesOrdenadas.filter { it.fechaRecogida > finHoyMs }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp)
    ) {
        // ENCABEZADO
        Text(
            text = "PANEL DE ADMINISTRACIÓN",
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = if (tabSeleccionada == 0) "Solicitudes Pendientes" else "Vehículos en Uso",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // PESTAÑAS
        TabRow(
            selectedTabIndex = tabSeleccionada,
            containerColor = SurfaceCard,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = tabSeleccionada == 0,
                onClick = { tabSeleccionada = 0 },
                text = { Text("PENDIENTES (${solicitudes.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = tabSeleccionada == 1,
                onClick = { tabSeleccionada = 1 },
                text = { Text("EN USO (${activos.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CONTENIDO SEGÚN LA PESTAÑA
        if (tabSeleccionada == 0) {
            if (solicitudes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    // SECCIÓN ENTREGAS DE HOY
                    if (entregasHoy.isNotEmpty()) {
                        item {
                            Text(
                                text = "ENTREGAS DE HOY (${entregasHoy.size})",
                                color = StatusOrangeGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        items(entregasHoy, key = { it.id }) { alquiler ->
                            SolicitudCard(
                                alquiler = alquiler,
                                esParaHoy = true,
                                onEntregar = { viewModel.aprobarReserva(alquiler) },
                                onRechazar = { viewModel.rechazarReserva(alquiler) }
                            )
                        }
                    }

                    // SECCIÓN PRÓXIMAS ENTREGAS
                    if (proximasEntregas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "PRÓXIMAS RESERVAS (${proximasEntregas.size})",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        items(proximasEntregas, key = { it.id }) { alquiler ->
                            SolicitudCard(
                                alquiler = alquiler,
                                esParaHoy = false,
                                onEntregar = { viewModel.aprobarReserva(alquiler) },
                                onRechazar = { viewModel.rechazarReserva(alquiler) }
                            )
                        }
                    }
                }
            }
        } else {
            // Pestaña de Activos
            if (activos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay vehículos en uso actualmente", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(activos, key = { it.id }) { alquiler ->
                        AlquilerActivoCard(
                            alquiler = alquiler,
                            onRecibirAuto = { viewModel.finalizarAlquiler(alquiler) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(
    alquiler: Alquiler,
    esParaHoy: Boolean,
    onEntregar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (esParaHoy) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(StatusOrangeGlow.copy(alpha = 0.8f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // AUTO, ESTADO Y MONTO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${alquiler.autoMarca} ${alquiler.autoModelo}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (esParaHoy) StatusOrangeGlow.copy(alpha = 0.12f)
                                    else BorderSubtle.copy(alpha = 0.3f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (esParaHoy) "ENTREGA HOY" else "PROGRAMADA",
                                color = if (esParaHoy) StatusOrangeGlow else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = Calculos.formatearMoneda(alquiler.costoTotal),
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // DATOS CLIENTE
                Text("Cliente: ${alquiler.nombreCliente}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Teléfono: ${alquiler.telefonoCliente}", color = TextSecondary, fontSize = 12.sp)
                Text("Correo: ${alquiler.correoCliente}", color = TextSecondary, fontSize = 12.sp)
                Text("DUI/Pasaporte: ${alquiler.documentoCliente} | Lic: ${alquiler.licenciaCliente}", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fechas: ${alquiler.fechaRecogidaTexto} - ${alquiler.fechaEntregaTexto} (${alquiler.diasTotales} días)",
                    color = if (esParaHoy) GoldPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // BOTONES DE ACCIÓN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RECHAZAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEntregar,
                        enabled = esParaHoy,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusGreenGlow,
                            disabledContainerColor = SurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (esParaHoy) "ENTREGAR VEHÍCULO" else "FECHA FUTURA",
                            color = if (esParaHoy) Color.Black else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlquilerActivoCard(
    alquiler: Alquiler,
    onRecibirAuto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = StatusRedGlow.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${alquiler.autoMarca} ${alquiler.autoModelo}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusRedGlow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "EN USO",
                        color = StatusRedGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Cliente: ${alquiler.nombreCliente}", color = TextPrimary, fontSize = 13.sp)
            Text("Teléfono: ${alquiler.telefonoCliente}", color = TextSecondary, fontSize = 12.sp)
            Text("Fecha Devolución: ${alquiler.fechaEntregaTexto}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(14.dp))

            // BOTÓN PARA RECIBIR
            Button(
                onClick = onRecibirAuto,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreenGlow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("RECIBIR VEHÍCULO (MARCAR DISPONIBLE)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}