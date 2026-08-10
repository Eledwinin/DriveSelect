package com.example.driveselect.ui.modulos.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.funciones.Calculos

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = viewModel()
) {
    val listaAlquileres by viewModel.historial.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Forzamos la actualización de datos cada vez que se entra a esta pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarHistorialUsuario()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
            .padding(16.dp)
    ) {
        Text(
            text = "MIS SOLICITUDES Y RENTAS",
            color = Color(0xFFFF9800),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Historial",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (listaAlquileres.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes solicitudes ni alquileres registrados.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaAlquileres) { alquiler ->
                    AlquilerItemCard(alquiler = alquiler)
                }
            }
        }
    }
}

@Composable
fun AlquilerItemCard(alquiler: Alquiler) {
    // Manejo de colores y estados de la solicitud
    val (colorEstado, textoEstado) = when (alquiler.estado.lowercase()) {
        "pendiente" -> Color(0xFFFF9800) to "PENDIENTE"
        "alquilado_en_proceso", "proceso", "aprobado" -> Color(0xFF2196F3) to "EN PROCESO"
        "finalizado" -> Color(0xFF4CAF50) to "FINALIZADO"
        "cancelado", "rechazado" -> Color(0xFFE53935) to "CANCELADO"
        else -> Color.Gray to alquiler.estado.uppercase()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181920)),
        modifier = Modifier.fillMaxWidth()
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
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = colorEstado.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFF2C2D3A), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("RECOGIDA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(alquiler.fechaRecogidaTexto, color = Color.White, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DEVOLUCIÓN", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(alquiler.fechaEntregaTexto, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${alquiler.diasTotales} día(s)",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = Calculos.formatearMoneda(alquiler.costoTotal),
                    color = Color(0xFFFF9800),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}