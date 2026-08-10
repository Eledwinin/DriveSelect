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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.driveselect.R
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.model.AutoEstado
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoListScreen(
    viewModel: InventarioViewModel,
    esAdmin: Boolean = false,
    onReservarClick: (Auto) -> Unit,
    onGestionClick: () -> Unit
) {
    val autos by viewModel.autos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ESTADO PARA EL MODAL DE DETALLES
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }

    // ESTADO LOCAL PARA EL TEXTO DE BÚSQUEDA
    var searchText by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // ESTADOS PARA EL MODAL DE DATOS FALTANTES
    var mostrarModalDatos by remember { mutableStateOf(false) }
    var autoPendienteReserva by remember { mutableStateOf<Auto?>(null) }

    var telefonoInput by remember { mutableStateOf("") }
    var duiInput by remember { mutableStateOf("") }
    var isGuardandoDatos by remember { mutableStateOf(false) }

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

    fun validarYProceder(auto: Auto) {
        val uid = currentUser?.uid ?: return

        FirebaseService.db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val tel = doc.getString("telefono") ?: ""
                val dui = doc.getString("dui") ?: ""

                if (tel.isBlank() || dui.isBlank()) {
                    autoPendienteReserva = auto
                    telefonoInput = tel
                    duiInput = dui
                    mostrarModalDatos = true
                } else {
                    onReservarClick(auto)
                }
            }
            .addOnFailureListener {
                onReservarClick(auto)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp)
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
                items(
                    items = autosFiltrados,
                    key = { auto -> auto.id.ifEmpty { auto.hashCode().toString() } }
                ) { auto ->
                    AutoCardItem(
                        auto = auto,
                        onCardClick = { autoDetalle = auto },
                        onReservarClick = { validarYProceder(auto) }
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
                    autoDetalle = null
                    validarYProceder(autoAProcesar)
                }
            )
        }
    }

    // MODAL DE COMPLETAR INFORMACIÓN DEL CLIENTE
    if (mostrarModalDatos) {
        AlertDialog(
            onDismissRequest = { mostrarModalDatos = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = "Completa tu información/Atualiza tu perfil",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Para continuar con la reserva requerimos tu teléfono y número de DUI.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = telefonoInput,
                        onValueChange = { nuevoTexto ->
                            // Solo permite caracteres numéricos y máximo 8 dígitos
                            if (nuevoTexto.all { it.isDigit() } && nuevoTexto.length <= 8) {
                                telefonoInput = nuevoTexto
                            }
                        },
                        label = { Text("Teléfono de Contacto", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = duiInput,
                        onValueChange = { duiInput = it },
                        label = { Text("Número de DUI", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = currentUser?.uid ?: ""
                        if (telefonoInput.isNotBlank() && duiInput.isNotBlank() && uid.isNotBlank()) {
                            isGuardandoDatos = true

                            val updates = mapOf(
                                "telefono" to telefonoInput.trim(),
                                "dui" to duiInput.trim()
                            )

                            FirebaseService.db.collection("usuarios").document(uid).update(updates)
                                .addOnSuccessListener {
                                    isGuardandoDatos = false
                                    mostrarModalDatos = false
                                    autoPendienteReserva?.let { auto ->
                                        onReservarClick(auto)
                                    }
                                }
                                .addOnFailureListener {
                                    isGuardandoDatos = false
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    enabled = !isGuardandoDatos && telefonoInput.isNotBlank() && duiInput.isNotBlank()
                ) {
                    if (isGuardandoDatos) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                        Text("GUARDAR Y CONTINUAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalDatos = false }) {
                    Text("CANCELAR", color = TextSecondary)
                }
            }
        )
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
            .clickable { onCardClick() },
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

        // FICHA TÉCNICA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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