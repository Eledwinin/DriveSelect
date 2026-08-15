package com.example.driveselect.ui.modulos.historial

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = viewModel()
) {
    val listaAlquileres by viewModel.historial.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var filtroSeleccionado by remember { mutableStateOf(0) }
    val categorias = listOf("TODOS", "PENDIENTES", "EN USO", "FINALIZADOS", "CANCELADOS")

    LaunchedEffect(Unit) {
        viewModel.cargarHistorialUsuario()
    }

    val listaFiltrada = remember(listaAlquileres, filtroSeleccionado) {
        when (filtroSeleccionado) {
            1 -> listaAlquileres.filter { it.estado.lowercase().trim() == "pendiente" }
            2 -> listaAlquileres.filter { it.estado.lowercase().trim() in listOf("en uso", "en_uso", "alquilado en uso", "alquilado_en_uso") }
            3 -> listaAlquileres.filter { it.estado.lowercase().trim() == "finalizado" }
            4 -> listaAlquileres.filter { it.estado.lowercase().trim() in listOf("cancelado", "rechazado") }
            else -> listaAlquileres
        }
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
            text = "MIS SOLICITUDES Y RENTAS",
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Historial",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // PESTAÑAS DE FILTRO
        ScrollableTabRow(
            selectedTabIndex = filtroSeleccionado,
            containerColor = SurfaceCard,
            contentColor = GoldPrimary,
            edgePadding = 8.dp,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            categorias.forEachIndexed { index, titulo ->
                val cantidad = when (index) {
                    1 -> listaAlquileres.count { it.estado.lowercase().trim() == "pendiente" }
                    2 -> listaAlquileres.count { it.estado.lowercase().trim() in listOf("en uso", "en_uso", "alquilado en uso", "alquilado_en_uso") }
                    3 -> listaAlquileres.count { it.estado.lowercase().trim() == "finalizado" }
                    4 -> listaAlquileres.count { it.estado.lowercase().trim() in listOf("cancelado", "rechazado") }
                    else -> listaAlquileres.size
                }

                Tab(
                    selected = filtroSeleccionado == index,
                    onClick = { filtroSeleccionado = index },
                    text = {
                        Text(
                            text = "$titulo ($cantidad)",
                            fontSize = 11.sp,
                            fontWeight = if (filtroSeleccionado == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CONTENIDO DE LA LISTA
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary, strokeWidth = 3.dp)
            }
        } else if (listaFiltrada.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay registros en esta sección.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = listaFiltrada, key = { it.id }) { alquiler ->
                    AlquilerItemCard(alquiler = alquiler)
                }
            }
        }
    }
}

@Composable
fun AlquilerItemCard(alquiler: Alquiler) {
    val (colorEstado, textoEstado) = when (alquiler.estado.lowercase().trim()) {
        "pendiente" -> StatusOrangeGlow to "PENDIENTE"
        "alquilado en proceso", "alquilado_en_proceso", "proceso", "aprobado" -> StatusOrangeGlow to "EN PROCESO"
        "en uso", "en_uso", "alquilado en uso", "alquilado_en_uso" -> StatusRedGlow to "EN USO"
        "finalizado" -> StatusGreenGlow to "FINALIZADO"
        "cancelado", "rechazado" -> Color(0xFFFF5252) to "CANCELADO"
        else -> TextSecondary to alquiler.estado.uppercase()
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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

                Surface(
                    color = colorEstado.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("RECOGIDA", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(alquiler.fechaRecogidaTexto, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DEVOLUCIÓN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(alquiler.fechaEntregaTexto, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${alquiler.diasTotales} día(s)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = Calculos.formatearMoneda(alquiler.costoTotal),
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}