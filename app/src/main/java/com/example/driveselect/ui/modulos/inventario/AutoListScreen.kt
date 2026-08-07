package com.example.driveselect.ui.modulos.inventario

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.driveselect.R
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.model.AutoEstado
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*

@Composable
fun AutoListScreen(
    viewModel: InventarioViewModel,
    onReservarClick: (Auto) -> Unit = {},
    onEntregarClick: (Auto) -> Unit = {},
    onDevolverClick: (Auto) -> Unit = {}
) {
    val autos by viewModel.autos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // ENCABEZADO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, Recepción",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "FLOTA VIP (${autos.size} AUTOS)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                    )
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "+ AGREGAR",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }

        // LISTA DE AUTOS
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary, strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(autos, key = { it.id }) { auto ->
                    AutoCardItem(
                        auto = auto,
                        onReservarClick = { onReservarClick(auto) },
                        onEntregarClick = { onEntregarClick(auto) },
                        onDevolverClick = { onDevolverClick(auto) }
                    )
                }
            }
        }
    }
}

@Composable
fun AutoCardItem(
    auto: Auto,
    onReservarClick: () -> Unit,
    onEntregarClick: () -> Unit,
    onDevolverClick: () -> Unit
) {
    val context = LocalContext.current

    // Carga segura del recurso drawable
    val drawableResId = remember(auto.imagenUrl) {
        try {
            if (auto.imagenUrl.isNotBlank()) {
                val id = context.resources.getIdentifier(
                    auto.imagenUrl.trim(),
                    "drawable",
                    context.packageName
                )
                if (id != 0) id else R.drawable.ic_launcher_background
            } else {
                R.drawable.ic_launcher_background
            }
        } catch (e: Exception) {
            R.drawable.ic_launcher_background
        }
    }

    val (colorEstado, textoEstado) = when (auto.estado) {
        AutoEstado.ALQUILADO_EN_PROCESO.displayName -> Pair(StatusOrangeGlow, "EN PROCESO")
        AutoEstado.ALQUILADO_EN_USO.displayName -> Pair(StatusRedGlow, "EN USO")
        else -> Pair(StatusGreenGlow, "DISPONIBLE")
    }

    val borderColor by animateColorAsState(
        targetValue = if (auto.estado == AutoEstado.ALQUILADO_EN_PROCESO.displayName) StatusOrangeGlow else BorderSubtle,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEN
            Box(
                modifier = Modifier
                    .size(95.dp, 70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(drawableResId)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${auto.marca} ${auto.modelo}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // DETALLES DEL AUTO
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${auto.marca} ${auto.modelo}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = Calculos.formatearMoneda(auto.precioPorDia),
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " / día",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorEstado.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorEstado)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // BOTONES DE ACCIÓN
            Box(
                contentAlignment = Alignment.Center
            ) {
                when (auto.estado) {
                    AutoEstado.DISPONIBLE.displayName -> {
                        BotonAccionGold(texto = "RENTAR", onClick = onReservarClick)
                    }

                    AutoEstado.ALQUILADO_EN_PROCESO.displayName -> {
                        OutlinedButton(
                            onClick = onEntregarClick,
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary))
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("ENTREGAR", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    AutoEstado.ALQUILADO_EN_USO.displayName -> {
                        Button(
                            onClick = onDevolverClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRedGlow),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("DEVOLVER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonAccionGold(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(28.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark)),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = texto, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}