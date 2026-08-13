package com.example.driveselect.ui.modulos.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 5.dp)
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
            // Pestaña de Pendientes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(solicitudes) { alquiler ->
                    SolicitudCard(
                        alquiler = alquiler,
                        onEntregar = { viewModel.aprobarReserva(alquiler) },
                        onRechazar = { viewModel.rechazarReserva(alquiler) }
                    )
                }
            }
        } else {
            // Pestaña de Activos
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activos) { alquiler ->
                    AlquilerActivoCard(
                        alquiler = alquiler,
                        onRecibirAuto = { viewModel.finalizarAlquiler(alquiler) }
                    )
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(
    alquiler: Alquiler,
    onEntregar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // AUTO Y MONTO
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
                Text(
                    text = Calculos.formatearMoneda(alquiler.costoTotal),
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // DATOS CLIENTE
            Text("Cliente: ${alquiler.nombreCliente}", color = TextPrimary, fontSize = 13.sp)
            Text("Teléfono: ${alquiler.telefonoCliente}", color = TextSecondary, fontSize = 12.sp)
            Text("Correo: ${alquiler.correoCliente}", color = TextSecondary, fontSize = 12.sp)
            Text("DUI/Pasaporte: ${alquiler.documentoCliente} | Lic: ${alquiler.licenciaCliente}", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Fechas: ${alquiler.fechaRecogidaTexto} - ${alquiler.fechaEntregaTexto} (${alquiler.diasTotales} días)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // BOTONES
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

                // Botón ajustado a ENTREGAR VEHÍCULO
                Button(
                    onClick = onEntregar,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreenGlow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENTREGAR VEHÍCULO", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${alquiler.autoMarca} ${alquiler.autoModelo}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "EN USO",
                    color = StatusRedGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Cliente: ${alquiler.nombreCliente}", color = TextPrimary, fontSize = 13.sp)
            Text("Fecha Devolución: ${alquiler.fechaEntregaTexto}", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

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