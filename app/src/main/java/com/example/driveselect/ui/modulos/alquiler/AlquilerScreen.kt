package com.example.driveselect.ui.modulos.alquiler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveselect.data.model.Auto
import com.example.driveselect.funciones.Calculos
import com.example.driveselect.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
object FechasFuturasSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val hoyUtc = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L)
        return utcTimeMillis >= hoyUtc
    }

    override fun isSelectableYear(docYear: Int): Boolean {
        return docYear >= 2026
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlquilerScreen(
    auto: Auto,
    viewModel: AlquilerViewModel,
    onReservaExitosa: () -> Unit
) {


    var nombreCliente by remember { mutableStateOf("") }
    var telefonoCliente by remember { mutableStateOf("") }
    var correoCliente by remember { mutableStateOf("") }
    var documentoCliente by remember { mutableStateOf("") }
    var licenciaCliente by remember { mutableStateOf("") }

    // las fechas son en milisegundos
    var fechaInicio by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var fechaFin by remember { mutableLongStateOf(System.currentTimeMillis() + (86400000L * 2)) } // +2 días por defecto

    var mostrarDatePickerInicio by remember { mutableStateOf(false) }
    var mostrarDatePickerFin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // calculos
    val diasTotales = remember(fechaInicio, fechaFin) {
        Calculos.calcularDiasDeAlquiler(fechaInicio, fechaFin)
    }
    val costoTotal = remember(diasTotales, auto.precioPorDia) {
        Calculos.calcularCostoTotal(diasTotales, auto.precioPorDia)
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp)
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

        Spacer(modifier = Modifier.height(20.dp))

        // FORMULARIO
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

                Spacer(modifier = Modifier.height(16.dp))

                //telefono
                OutlinedTextField(
                    value = telefonoCliente,
                    onValueChange = { telefonoCliente = it },
                    label = { Text("Teléfono de Contacto", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                //correo
                OutlinedTextField(
                    value = correoCliente,
                    onValueChange = { correoCliente = it },
                    label = { Text("Correo Electrónico", color = TextSecondary) },
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

                //dui
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                Spacer(modifier = Modifier.height(10.dp))

                // selector Fecha Inicio
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { mostrarDatePickerInicio = true }
                        .padding(14.dp)
                ) {
                    Text("FECHA PARA RECOGER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Calculos.formatearFecha(fechaInicio),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // selector Fecha entrega
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { mostrarDatePickerFin = true }
                        .padding(14.dp)
                ) {
                    Text("FECHA DE DEVOLUCIÓN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Calculos.formatearFecha(fechaFin),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = BorderSubtle, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen de Cálculo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Días calculados:", color = TextSecondary, fontSize = 13.sp)
                    Text("$diasTotales día(s)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Costo Total:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = Calculos.formatearMoneda(costoTotal),
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // BOTÓN CONFIRMAR
        Button(
            onClick = {
                if (nombreCliente.isNotBlank()) {
                    isLoading = true
                    viewModel.procesarReserva(
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
                            onReservaExitosa()
                        }
                    )
                }
            },
            enabled = !isLoading && nombreCliente.isNotBlank(),
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
                    Text("CONFIRMAR RESERVA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }

    // Modal Fecha Inicio
    if (mostrarDatePickerInicio) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicio,
            selectableDates = FechasFuturasSelectableDates //esto es para bloqeuar dias que ya pasaron
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { nuevaFecha ->
                        fechaInicio = nuevaFecha
                        // Si la fecha de devolución quedó antes que la nueva fecha de recogida, se ajusta
                        if (fechaFin < nuevaFecha) {
                            fechaFin = nuevaFecha + 86400000L
                        }
                    }
                    mostrarDatePickerInicio = false
                }) { Text("Aceptar", color = GoldPrimary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Modal Fecha Fin
    if (mostrarDatePickerFin) {
        // la fecha de devolución solo puede ser igual o despues a la fecha de recogida
        val reglaFechaFin = remember(fechaInicio) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val inicioUtc = fechaInicio - (fechaInicio % 86400000L)
                    return utcTimeMillis >= inicioUtc
                }
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaFin,
            selectableDates = reglaFechaFin // loquea días anteriores a la fecha de recogida
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { nuevaFecha ->
                        fechaFin = nuevaFecha
                    }
                    mostrarDatePickerFin = false
                }) { Text("Aceptar", color = GoldPrimary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}