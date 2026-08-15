package com.example.driveselect.ui.modulos.alquiler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlquilerScreen(
    auto: Auto,
    esAdmin: Boolean = false,
    viewModel: AlquilerViewModel,
    onReservaExitosa: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // OBTENER RECURSO DE IMAGEN
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

    // CAMPOS AUTO-COMPLETADOS DESDE FIRESTORE
    var nombreCliente by remember { mutableStateOf(if (esAdmin) "" else (currentUser?.displayName ?: "")) }
    var correoCliente by remember { mutableStateOf(if (esAdmin) "" else (currentUser?.email ?: "")) }
    var telefonoCliente by remember { mutableStateOf("") }
    var documentoCliente by remember { mutableStateOf("") }
    var licenciaCliente by remember { mutableStateOf("") }

    // Cargar datos guardados del perfil
    LaunchedEffect(currentUser?.uid, esAdmin) {
        if (!esAdmin) {
            currentUser?.uid?.let { uid ->
                FirebaseService.db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val nom = doc.getString("nombre") ?: ""
                            val tel = doc.getString("telefono") ?: ""
                            val dui = doc.getString("dui") ?: ""
                            val lic = doc.getString("licencia") ?: ""

                            if (nom.isNotBlank()) nombreCliente = nom
                            if (tel.isNotBlank()) telefonoCliente = tel
                            if (dui.isNotBlank()) documentoCliente = dui
                            if (lic.isNotBlank()) licenciaCliente = lic
                        }
                    }
            }
        }
    }

    // varibble para el rango de fechas que esta ocupado el auto
    var rangosOcupados by remember { mutableStateOf<List<Pair<Long, Long>>>(emptyList()) }


    LaunchedEffect(auto.id) {
        if (auto.id.isBlank()) return@LaunchedEffect

        try {
            val lista = mutableListOf<Pair<Long, Long>>()

            // busca en la colección "solicitudes"
            val snapSolicitudes = FirebaseService.db.collection("solicitudes").get().await()
            for (doc in snapSolicitudes.documents) {
                val idAutoDoc = doc.getString("autoId") ?: doc.getString("idAuto") ?: doc.getString("auto_id") ?: ""
                val estado = (doc.getString("estado") ?: "").uppercase().trim()

                if (idAutoDoc == auto.id && estado in listOf("PENDIENTE", "APROBADO", "EN USO", "EN_USO", "ALQUILADO EN PROCESO", "ALQUILADO EN USO")) {
                    val inicio = doc.getLong("fechaInicio") ?: doc.getLong("fechaRecogida") ?: continue
                    val fin = doc.getLong("fechaFin") ?: doc.getLong("fechaEntrega") ?: continue
                    lista.add(Pair(inicio, fin))
                }
            }

            // busca en la colección "alquileres"
            val snapAlquileres = FirebaseService.db.collection("alquileres").get().await()
            for (doc in snapAlquileres.documents) {
                val idAutoDoc = doc.getString("autoId") ?: doc.getString("idAuto") ?: doc.getString("auto_id") ?: ""
                val estado = (doc.getString("estado") ?: "").uppercase().trim()

                if (idAutoDoc == auto.id && estado in listOf("PENDIENTE", "APROBADO", "EN USO", "EN_USO", "ALQUILADO EN PROCESO", "ALQUILADO EN USO")) {
                    val inicio = doc.getLong("fechaInicio") ?: doc.getLong("fechaRecogida") ?: continue
                    val fin = doc.getLong("fechaFin") ?: doc.getLong("fechaEntrega") ?: continue
                    lista.add(Pair(inicio, fin))
                }
            }

            rangosOcupados = lista
        } catch (e: Exception) {
            rangosOcupados = emptyList()
        }
    }

    // Fechas en milisegundos
    var fechaInicio by remember { mutableLongStateOf(0L) }
    var fechaFin by remember { mutableLongStateOf(0L) }

    var mostrarDatePickerInicio by remember { mutableStateOf(false) }
    var mostrarDatePickerFin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Cálculos
    val diasTotales = remember(fechaInicio, fechaFin) {
        if (fechaInicio > 0L && fechaFin > 0L) {
            Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin)
        } else {
            0
        }
    }

val costoTotal = remember(diasTotales, auto.precioPorDia) {
    if (diasTotales > 0) {
        Calculos.calcularCostoTotal(diasTotales, auto.precioPorDia)
    } else {
        0.0
    }
}

    // regla para bloquear dias deshabilitados en el calendario
    val reglaFechasDisponibles = remember(rangosOcupados) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return !Calculos.esFechaOcupada(utcTimeMillis, rangosOcupados)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ENCABEZADO
        Text(
            text = "SOLICITUD DE RENTA",
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "${auto.marca} ${auto.modelo}",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "${Calculos.formatearMoneda(auto.precioPorDia)} por día",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // TARJETA DE VISTA PREVIA DEL AUTO
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FORMULARIO DE DATOS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Nombre del Cliente
                OutlinedTextField(
                    value = nombreCliente,
                    onValueChange = { nombreCliente = it },
                    label = { Text("Nombre del Cliente", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Teléfono
                OutlinedTextField(
                    value = telefonoCliente,
                    onValueChange = { nuevoTexto ->
                        if (nuevoTexto.all { it.isDigit() } && nuevoTexto.length <= 8) {
                            telefonoCliente = nuevoTexto
                        }
                    },
                    label = { Text("Teléfono", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Correo
                OutlinedTextField(
                    value = correoCliente,
                    onValueChange = { correoCliente = it },
                    label = { Text("Correo", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // DUI y Licencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = documentoCliente,
                        onValueChange = { documentoCliente = it },
                        label = { Text("DUI / Pasaporte", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = licenciaCliente,
                        onValueChange = { licenciaCliente = it },
                        label = { Text("N° Licencia", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector Fecha Inicio
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { mostrarDatePickerInicio = true }
                        .padding(12.dp)
                ) {
                    Text("FECHA PARA RECOGER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (fechaInicio > 0L) Calculos.formatearFecha(fechaInicio) else "Seleccionar fecha de inicio",
                        color = if (fechaInicio > 0L) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector Fecha Devolución
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { mostrarDatePickerFin = true }
                        .padding(12.dp)
                ) {
                    Text("FECHA DE DEVOLUCIÓN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (fechaFin > 0L) Calculos.formatearFecha(fechaFin) else "Seleccionar fecha de devolución",
                        color = if (fechaInicio > 0L) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Días calculados:", color = TextSecondary, fontSize = 13.sp)
                    Text("$diasTotales día(s)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Costo Total:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = Calculos.formatearMoneda(costoTotal),
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BOTÓN CONFIRMAR RESERVA
        Button(
            onClick = {
                val currentUserId = currentUser?.uid ?: ""

                if (nombreCliente.isNotBlank() && telefonoCliente.isNotBlank() && documentoCliente.isNotBlank()) {
                    isLoading = true

                    if (currentUserId.isNotBlank()) {
                        val datosUsuario = mapOf(
                            "telefono" to telefonoCliente.trim(),
                            "dui" to documentoCliente.trim(),
                            "licencia" to licenciaCliente.trim()
                        )
                        FirebaseService.db.collection("usuarios").document(currentUserId).update(datosUsuario)
                    }

                    viewModel.procesarReserva(
                        auto = auto,
                        usuarioId = currentUserId,
                        nombreCliente = nombreCliente,
                        telefonoCliente = telefonoCliente,
                        duiCliente = documentoCliente,
                        licenciaCliente = licenciaCliente,
                        correoCliente = correoCliente,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        onExito = {
                            isLoading = false
                            onReservaExitosa()
                        }
                    )
                }
            },
            enabled = !isLoading && nombreCliente.isNotBlank() && telefonoCliente.isNotBlank() && documentoCliente.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark)),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                } else {
                    Text("CONFIRMAR RESERVA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    // Modal Fecha Inicio
    if (mostrarDatePickerInicio) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (fechaInicio > 0L) fechaInicio else null,
            selectableDates = reglaFechasDisponibles
        )

        val esFechaValida = datePickerState.selectedDateMillis?.let { utcMillis ->
            reglaFechasDisponibles.isSelectableDate(utcMillis)
        } ?: false

        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerInicio = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val offset = TimeZone.getDefault().getOffset(utcMillis)
                            val localMillis = utcMillis - offset

                            fechaInicio = localMillis
                            if (fechaFin <= localMillis) {
                                fechaFin = 0L
                            }
                        }
                        mostrarDatePickerInicio = false
                    },
                    enabled = esFechaValida
                ) {
                    Text(
                        "Aceptar",
                        color = if (esFechaValida) GoldPrimary else TextSecondary.copy(alpha = 0.4f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerInicio = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Modal Fecha Fin
    if (mostrarDatePickerFin) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (fechaFin > 0L) fechaFin else null,
            selectableDates = reglaFechasDisponibles
        )

        val esFechaValida = datePickerState.selectedDateMillis?.let { utcMillis ->
            val offset = TimeZone.getDefault().getOffset(utcMillis)
            val localMillis = utcMillis - offset
            reglaFechasDisponibles.isSelectableDate(utcMillis) && (fechaInicio == 0L || localMillis > fechaInicio)
        } ?: false

        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFin = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val offset = TimeZone.getDefault().getOffset(utcMillis)
                            val localMillis = utcMillis - offset

                            fechaFin = localMillis
                        }
                        mostrarDatePickerFin = false
                    },
                    enabled = esFechaValida
                ) {
                    Text(
                        "Aceptar",
                        color = if (esFechaValida) GoldPrimary else TextSecondary.copy(alpha = 0.4f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFin = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}