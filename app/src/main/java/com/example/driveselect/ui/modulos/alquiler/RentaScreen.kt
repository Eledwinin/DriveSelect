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
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.driveselect.funciones.Validaciones
import com.example.driveselect.ui.modulos.mensajes.BannerMensajeFlotante
import com.example.driveselect.ui.modulos.mensajes.DialogoMensaje
import com.example.driveselect.ui.modulos.mensajes.TipoMensaje
import com.example.driveselect.ui.theme.*
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentaScreen(
    auto: Auto,
    viewModel: RentaViewModel,
    onRentaExitosa: () -> Unit,
    onVolver: () -> Unit = {}
) {
    val context = LocalContext.current

    var mostrarExitoDialog by remember { mutableStateOf(false) }
    var mostrarBannerAdvertencia by remember { mutableStateOf(false) }
    var mensajeAdvertencia by remember { mutableStateOf("") }
    var mostrarBannerError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val drawableResId = remember(auto.imagenUrl) {
        try {
            if (auto.imagenUrl.isNotBlank()) {
                val id = context.resources.getIdentifier(auto.imagenUrl.trim(), "drawable", context.packageName)
                if (id != 0) id else R.drawable.ic_launcher_background
            } else R.drawable.ic_launcher_background
        } catch (e: Exception) { R.drawable.ic_launcher_background }
    }

    var nombreCliente by remember { mutableStateOf("") }
    var correoCliente by remember { mutableStateOf("") }
    var telefonoCliente by remember { mutableStateOf("") }
    var documentoCliente by remember { mutableStateOf("") }
    var licenciaCliente by remember { mutableStateOf("") }

    var nombreTocado by remember { mutableStateOf(false) }
    var telefonoTocado by remember { mutableStateOf(false) }
    var correoTocado by remember { mutableStateOf(false) }
    var documentoTocado by remember { mutableStateOf(false) }
    var licenciaTocado by remember { mutableStateOf(false) }

    val nombreError = nombreTocado && !Validaciones.esNombreValido(nombreCliente)
    val telefonoError = telefonoTocado && !Validaciones.esTelefonoValido(telefonoCliente)
    val correoError = correoTocado && correoCliente.isNotBlank() && !Validaciones.esCorreoValido(correoCliente)
    val documentoError = documentoTocado && !Validaciones.esDocumentoValido(documentoCliente)
    val licenciaError = licenciaTocado && licenciaCliente.trim().length < 4

    val fechaInicio = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }.timeInMillis

    var fechaFin by remember { mutableLongStateOf(0L) }
    var mostrarDatePickerFin by remember { mutableStateOf(false) }
    var mostrarModalChecklist by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var rangosOcupados by remember { mutableStateOf<List<Pair<Long, Long>>>(emptyList()) }

    LaunchedEffect(auto.id) {
        if (auto.id.isBlank()) return@LaunchedEffect
        try {
            val lista = mutableListOf<Pair<Long, Long>>()
            val snapAlquileres = FirebaseService.db.collection("alquileres").get().await()
            for (doc in snapAlquileres.documents) {
                val idAutoDoc = doc.getString("autoId") ?: ""
                val estado = (doc.getString("estado") ?: "").lowercase().trim()

                if (idAutoDoc == auto.id && estado in listOf("pendiente", "aprobado", "en uso", "en_uso", "alquilado en proceso", "alquilado en uso")) {
                    val inicio = doc.getLong("fechaRecogida") ?: doc.getLong("fechaInicio") ?: continue
                    val fin = doc.getLong("fechaEntrega") ?: doc.getLong("fechaFin") ?: continue
                    lista.add(Pair(inicio, fin))
                }
            }
            rangosOcupados = lista
        } catch (e: Exception) {
            rangosOcupados = emptyList()
        }
    }

    val diasTotales = remember(fechaInicio, fechaFin) {
        if (fechaFin > 0L) Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin) else 0
    }

    val costoTotal = remember(diasTotales, auto.precioPorDia) {
        if (diasTotales > 0) Calculos.calcularCostoTotal(diasTotales, auto.precioPorDia) else 0.0
    }

    val reglaFechasDisponibles = remember(rangosOcupados) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return !Calculos.esFechaOcupada(utcTimeMillis, rangosOcupados)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "REGISTRO DE RENTA EN SUCURSAL",
                color = StatusGreenGlow,
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
                        model = ImageRequest.Builder(context).data(drawableResId).crossfade(true).build(),
                        contentDescription = "${auto.marca} ${auto.modelo}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nombreCliente,
                        onValueChange = { nombreCliente = it },
                        label = { Text("Nombre Completo del Cliente", color = TextSecondary) },
                        singleLine = true,
                        isError = nombreError,
                        supportingText = {
                            if (nombreError) {
                                Text("Ingresa un nombre válido (mínimo 3 caracteres)", color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGreenGlow,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && nombreCliente.isNotEmpty()) {
                                    nombreTocado = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = telefonoCliente,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 8) telefonoCliente = it },
                        label = { Text("Teléfono", color = TextSecondary) },
                        singleLine = true,
                        isError = telefonoError,
                        supportingText = {
                            if (telefonoError) {
                                Text("El teléfono debe tener exactamente 8 dígitos", color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGreenGlow,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && telefonoCliente.isNotEmpty()) {
                                    telefonoTocado = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = correoCliente,
                        onValueChange = { correoCliente = it },
                        label = { Text("Correo Electrónico (Opcional)", color = TextSecondary) },
                        singleLine = true,
                        isError = correoError,
                        supportingText = {
                            if (correoError) {
                                Text("Formato de correo no válido (ejemplo@correo.com)", color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGreenGlow,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && correoCliente.isNotEmpty()) {
                                    correoTocado = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = documentoCliente,
                            onValueChange = { documentoCliente = it },
                            label = { Text("DUI / Pasaporte", color = TextSecondary) },
                            singleLine = true,
                            isError = documentoError,
                            supportingText = {
                                if (documentoError) {
                                    Text("Mínimo 8 caracteres", color = Color(0xFFFF5252), fontSize = 10.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StatusGreenGlow,
                                unfocusedBorderColor = BorderSubtle,
                                errorBorderColor = Color(0xFFFF5252),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                errorTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && documentoCliente.isNotEmpty()) {
                                        documentoTocado = true
                                    }
                                }
                        )
                        OutlinedTextField(
                            value = licenciaCliente,
                            onValueChange = { licenciaCliente = it },
                            label = { Text("N° Licencia", color = TextSecondary) },
                            singleLine = true,
                            isError = licenciaError,
                            supportingText = {
                                if (licenciaError) {
                                    Text("Mínimo 4 caracteres", color = Color(0xFFFF5252), fontSize = 10.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StatusGreenGlow,
                                unfocusedBorderColor = BorderSubtle,
                                errorBorderColor = Color(0xFFFF5252),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                errorTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && licenciaCliente.isNotEmpty()) {
                                        licenciaTocado = true
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text("FECHA DE ENTREGA (HOY)", color = StatusGreenGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Calculos.formatearFecha(fechaInicio),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, if (fechaFin > 0L) StatusGreenGlow else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { mostrarDatePickerFin = true }
                            .padding(12.dp)
                    ) {
                        Text("FECHA PROGRAMADA DE DEVOLUCIÓN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (fechaFin > 0L) Calculos.formatearFecha(fechaFin) else "Seleccionar fecha de retorno",
                            color = if (fechaFin > 0L) TextPrimary else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Días calculados:", color = TextSecondary, fontSize = 13.sp)
                        Text("$diasTotales día(s)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total a Cobrar:", color = TextSecondary, fontSize = 14.sp)
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

            Button(
                onClick = {
                    nombreTocado = true
                    telefonoTocado = true
                    correoTocado = true
                    documentoTocado = true
                    licenciaTocado = true

                    if (!Validaciones.esNombreValido(nombreCliente)) {
                        mensajeAdvertencia = "Por favor ingresa el nombre completo del cliente (mínimo 3 caracteres)."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    if (!Validaciones.esTelefonoValido(telefonoCliente)) {
                        mensajeAdvertencia = "El teléfono debe contener exactamente 8 números."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    if (correoCliente.isNotBlank() && !Validaciones.esCorreoValido(correoCliente)) {
                        mensajeAdvertencia = "El correo electrónico no tiene un formato válido."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    if (!Validaciones.esDocumentoValido(documentoCliente)) {
                        mensajeAdvertencia = "El DUI / Pasaporte debe tener al menos 8 caracteres."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    if (licenciaCliente.trim().length < 4) {
                        mensajeAdvertencia = "Por favor ingresa el número de licencia del cliente."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    if (fechaFin == 0L) {
                        mensajeAdvertencia = "Debes seleccionar la fecha programada de devolución del vehículo."
                        mostrarBannerAdvertencia = true
                        return@Button
                    }
                    mostrarModalChecklist = true
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreenGlow,
                    disabledContainerColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "VALIDAR Y ENTREGAR VEHÍCULO",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        BannerMensajeFlotante(
            visible = mostrarBannerAdvertencia,
            tipo = TipoMensaje.ADVERTENCIA,
            mensaje = mensajeAdvertencia,
            onDismiss = { mostrarBannerAdvertencia = false }
        )

        BannerMensajeFlotante(
            visible = mostrarBannerError,
            tipo = TipoMensaje.ERROR,
            mensaje = mensajeError,
            onDismiss = { mostrarBannerError = false }
        )
    }

    DialogoMensaje(
        visible = mostrarExitoDialog,
        tipo = TipoMensaje.EXITO,
        titulo = "¡Renta Activada!",
        mensaje = "El vehículo ${auto.marca} ${auto.modelo} fue entregado con éxito y su estado ha cambiado a EN USO.",
        textoBoton = "FINALIZAR Y SALIR",
        onDismiss = {
            mostrarExitoDialog = false
            onRentaExitosa()
        }
    )

    if (mostrarDatePickerFin) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (fechaFin > 0L) fechaFin else null,
            selectableDates = reglaFechasDisponibles
        )

        val esFechaValida = datePickerState.selectedDateMillis?.let { utcMillis ->
            val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
            val calLocal = Calendar.getInstance().apply {
                set(calUtc.get(Calendar.YEAR), calUtc.get(Calendar.MONTH), calUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            reglaFechasDisponibles.isSelectableDate(utcMillis) && calLocal.timeInMillis > fechaInicio
        } ?: false

        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFin = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
                            val calLocal = Calendar.getInstance().apply {
                                set(calUtc.get(Calendar.YEAR), calUtc.get(Calendar.MONTH), calUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            fechaFin = calLocal.timeInMillis
                        }
                        mostrarDatePickerFin = false
                    },
                    enabled = esFechaValida
                ) {
                    Text("Aceptar", color = if (esFechaValida) GoldPrimary else TextSecondary.copy(alpha = 0.4f))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFin = false }) { Text("Cancelar", color = TextSecondary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarModalChecklist) {
        var checkDocumentos by remember { mutableStateOf(false) }
        var checkContrato by remember { mutableStateOf(false) }
        var checkPago by remember { mutableStateOf(false) }
        val requisitosCompletos = checkDocumentos && checkContrato && checkPago

        AlertDialog(
            onDismissRequest = { mostrarModalChecklist = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Checklist de Entrega Inmediata",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Confirma los requisitos físicos antes de entregar la llave:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Documentos físicos", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("DUI y Licencia verificados", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = checkDocumentos,
                            onCheckedChange = { checkDocumentos = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = StatusGreenGlow, checkedThumbColor = Color.Black)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contrato firmado", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Firma física completada", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = checkContrato,
                            onCheckedChange = { checkContrato = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = StatusGreenGlow, checkedThumbColor = Color.Black)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pago recibido", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Cobro de ${Calculos.formatearMoneda(costoTotal)}", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = checkPago,
                            onCheckedChange = { checkPago = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = StatusGreenGlow, checkedThumbColor = Color.Black)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarModalChecklist = false
                        isLoading = true
                        viewModel.procesarRentaInmediata(
                            auto = auto,
                            nombreCliente = nombreCliente,
                            telefonoCliente = telefonoCliente,
                            duiCliente = documentoCliente,
                            licenciaCliente = licenciaCliente,
                            correoCliente = correoCliente,
                            fechaInicio = fechaInicio,
                            fechaFin = fechaFin,
                            onExito = {
                                isLoading = false
                                mostrarExitoDialog = true
                            },
                            onError = { err ->
                                isLoading = false
                                mensajeError = err
                                mostrarBannerError = true
                            }
                        )
                    },
                    enabled = requisitosCompletos,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusGreenGlow,
                        disabledContainerColor = SurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "ENTREGAR LLAVES Y ACTIVAR RENTA",
                        color = if (requisitosCompletos) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalChecklist = false }) {
                    Text("CANCELAR", color = TextSecondary)
                }
            }
        )
    }
}