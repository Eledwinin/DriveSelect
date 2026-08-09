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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoListScreen(
    viewModel: InventarioViewModel,
    onReservarClick: (Auto) -> Unit = {},
    onGestionClick: () -> Unit = {}
) {
    val autos by viewModel.autos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ESTADO PARA EL MODAL DE DETALLES
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }

    // ESTADO LOCAL PARA EL TEXTO DE BÚSQUEDA
    var searchText by remember { mutableStateOf("") }

    // FILTRADO DINÁMICO
    val autosFiltrados = remember(autos, searchText) {
        if (searchText.isBlank()) {
            autos
        } else {
            autos.filter { auto ->
                auto.marca.contains(searchText, ignoreCase = true) ||
                        auto.modelo.contains(searchText, ignoreCase = true)
            }
        }
    }

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
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Catálogo",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "FLOTA VIP (${autosFiltrados.size} DE ${autos.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // BOTÓN ACCESO ADMIN
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                    )
                    .clickable { onGestionClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "SOLICITUDES",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
        }

        // BUSCADOR EN EL ENCABEZADO
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text(
                    text = "Buscar por marca o modelo...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    Text(
                        text = "✕",
                        color = TextSecondary,
                        modifier = Modifier
                            .clickable { searchText = "" }
                            .padding(8.dp)
                    )
                } else {
                    Text(text = "🔍", fontSize = 14.sp)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // LISTA DE AUTOS
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary, strokeWidth = 3.dp)
            }
        } else if (autosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron vehículos",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(autosFiltrados, key = { it.id }) { auto ->
                    AutoCardItem(
                        auto = auto,
                        onCardClick = { autoDetalle = auto }, // ABRE EL MODAL AL HACER CLIC EN LA TARJETA
                        onReservarClick = { onReservarClick(auto) }
                    )
                }
            }
        }
    }

    // MODAL DE DETALLES DEL VEHÍCULO
    autoDetalle?.let { auto ->
        ModalBottomSheet(
            onDismissRequest = { autoDetalle = null },
            containerColor = SurfaceCard,
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ModalDetalleAutoContent(
                auto = auto,
                onReservarClick = {
                    val autoAProcesar = auto
                    autoDetalle = null // Cerrar modal
                    onReservarClick(autoAProcesar) // Ir a alquiler
                }
            )
        }
    }
}

@Composable
fun AutoCardItem(
    auto: Auto,
    onCardClick: () -> Unit,
    onReservarClick: () -> Unit
) {
    val context = LocalContext.current

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
        AutoEstado.ALQUILADO_EN_PROCESO.displayName -> Pair(StatusOrangeGlow, "ALQUILADO EN PROCESO")
        AutoEstado.ALQUILADO_EN_USO.displayName -> Pair(StatusRedGlow, "ALQUILADO EN USO")
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
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable { onCardClick() }, // CLIC EN TODA LA TARJETA
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

            // ACCIONES SEGÚN ESTADO
            Box(contentAlignment = Alignment.Center) {
                if (auto.estado == AutoEstado.DISPONIBLE.displayName) {
                    BotonAccionGold(texto = "RENTAR", onClick = onReservarClick)
                } else {
                    Text(
                        text = "NO DISPONIBLE",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// COMPONENTES AUXILIARES
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

// DISEÑO DEL CONTENIDO DENTRO DEL MODAL
@Composable
private fun ModalDetalleAutoContent(
    auto: Auto,
    onReservarClick: () -> Unit
) {
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // IMAGEN DEL VEHÍCULO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // MARCA Y MODELO
        Text(
            text = auto.marca.uppercase(),
            color = GoldPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = auto.modelo,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // PRECIO
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = Calculos.formatearMoneda(auto.precioPorDia),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = " / día",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FICHA TÉCNICA (MOTOR Y ESTADO TRADUCIDO DE FIRESTORE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // MUESTRA EL MOTOR DIRECTAMENTE DESDE LA DB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Motor:", color = TextSecondary, fontSize = 12.sp)
                Text(
                    text = auto.motor.ifBlank { "No especificado" },
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

            // ESTADO DEL VEHÍCULO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estado:", color = TextSecondary, fontSize = 12.sp)
                Text(
                    text = auto.estado,
                    color = if (auto.estado == AutoEstado.DISPONIBLE.displayName) StatusGreenGlow else StatusRedGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BOTÓN RESERVAR SI ESTÁ DISPONIBLE
        if (auto.estado == AutoEstado.DISPONIBLE.displayName) {
            Button(
                onClick = onReservarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(
                    text = "CONTINUAR A RESERVA",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}