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
import com.example.driveselect.ui.modulos.mensajes.BannerMensajeFlotante
import com.example.driveselect.ui.modulos.mensajes.DialogoMensaje
import com.example.driveselect.ui.modulos.mensajes.TipoMensaje
import com.example.driveselect.ui.theme.*
import java.util.Calendar

@Composable
fun GestionSolicitudesScreen(
    viewModel: GestionViewModel,
    onVolverClick: () -> Unit = {}
) {
    val solicitudes by viewModel.solicitudesPendientes.collectAsState()
    val activos by viewModel.alquileresActivos.collectAsState()
    var tabSeleccionada by remember { mutableStateOf(0) }

    var solicitudParaEntregar by remember { mutableStateOf<Alquiler?>(null) }
    var alquilerParaRecibir by remember { mutableStateOf<Alquiler?>(null) }

    var dialogoExitoVisible by remember { mutableStateOf(false) }
    var tituloExito by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }
    var bannerNotificacionVisible by remember { mutableStateOf(false) }
    var mensajeBanner by remember { mutableStateOf("") }

    val calHoyInicio = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val inicioHoyMs = calHoyInicio.timeInMillis

    val calHoyFin = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
    val finHoyMs = calHoyFin.timeInMillis

    val solicitudesOrdenadas = remember(solicitudes) { solicitudes.sortedBy { it.fechaRecogida } }
    val entregasHoy = remember(solicitudesOrdenadas) { solicitudesOrdenadas.filter { it.fechaRecogida <= finHoyMs } }
    val proximasEntregas = remember(solicitudesOrdenadas) { solicitudesOrdenadas.filter { it.fechaRecogida > finHoyMs } }

    val activosOrdenados = remember(activos) { activos.sortedBy { it.fechaEntrega } }
    val vencidosConMora = remember(activosOrdenados) { activosOrdenados.filter { it.fechaEntrega < inicioHoyMs } }
    val devolucionesHoy = remember(activosOrdenados) { activosOrdenados.filter { it.fechaEntrega in inicioHoyMs..finHoyMs } }
    val proximasDevoluciones = remember(activosOrdenados) { activosOrdenados.filter { it.fechaEntrega > finHoyMs } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp)
        ) {
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
                                    onEntregar = { solicitudParaEntregar = alquiler },
                                    onRechazar = {
                                        viewModel.rechazarReserva(alquiler) {
                                            mensajeBanner = "Solicitud rechazada correctamente"
                                            bannerNotificacionVisible = true
                                        }
                                    }
                                )
                            }
                        }

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
                                    onEntregar = { solicitudParaEntregar = alquiler },
                                    onRechazar = {
                                        viewModel.rechazarReserva(alquiler) {
                                            mensajeBanner = "Solicitud rechazada correctamente"
                                            bannerNotificacionVisible = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                if (activos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay vehículos en uso actualmente", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        if (vencidosConMora.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🚨 RETRASOS / VENCIDOS CON MORA (${vencidosConMora.size})",
                                    color = Color(0xFFFF5252),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            items(vencidosConMora, key = { it.id }) { alquiler ->
                                AlquilerActivoCard(
                                    alquiler = alquiler,
                                    tipoEstado = TipoEstadoActivo.VENCIDO,
                                    onRecibirAuto = { alquilerParaRecibir = alquiler }
                                )
                            }
                        }

                        if (devolucionesHoy.isNotEmpty()) {
                            item {
                                if (vencidosConMora.isNotEmpty()) Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "DEVOLUCIONES DE HOY (${devolucionesHoy.size})",
                                    color = StatusOrangeGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            items(devolucionesHoy, key = { it.id }) { alquiler ->
                                AlquilerActivoCard(
                                    alquiler = alquiler,
                                    tipoEstado = TipoEstadoActivo.HOY,
                                    onRecibirAuto = { alquilerParaRecibir = alquiler }
                                )
                            }
                        }

                        if (proximasDevoluciones.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "CONTRATO VIGENTE / EN TIEMPO (${proximasDevoluciones.size})",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            items(proximasDevoluciones, key = { it.id }) { alquiler ->
                                AlquilerActivoCard(
                                    alquiler = alquiler,
                                    tipoEstado = TipoEstadoActivo.VIGENTE,
                                    onRecibirAuto = { alquilerParaRecibir = alquiler }
                                )
                            }
                        }
                    }
                }
            }
        }

        BannerMensajeFlotante(
            visible = bannerNotificacionVisible,
            tipo = TipoMensaje.INFO,
            mensaje = mensajeBanner,
            onDismiss = { bannerNotificacionVisible = false }
        )
    }

    solicitudParaEntregar?.let { alquiler ->
        DialogoChecklistEntrega(
            alquiler = alquiler,
            onDismiss = { solicitudParaEntregar = null },
            onConfirmarEntrega = {
                viewModel.aprobarReserva(alquiler) {
                    tituloExito = "¡Vehículo Entregado!"
                    mensajeExito = "Se ha completado la entrega de ${alquiler.autoMarca} ${alquiler.autoModelo}"
                    dialogoExitoVisible = true
                }
                solicitudParaEntregar = null
            }
        )
    }

    alquilerParaRecibir?.let { alquiler ->
        DialogoRecepcionVehiculo(
            alquiler = alquiler,
            onDismiss = { alquilerParaRecibir = null },
            onConfirmarRecepcion = {
                val diasMora = Calculos.calcularDiasMora(alquiler.fechaEntrega)
                val mora = Calculos.calcularMontoMora(diasMora)
                val totalFinal = alquiler.costoTotal + mora

                viewModel.finalizarAlquiler(alquiler) {
                    tituloExito = "¡Vehículo Recibido!"
                    mensajeExito = if (diasMora > 0) {
                        "Auto recibido exitosamente. Total cobrado: ${Calculos.formatearMoneda(totalFinal)} (incluye ${Calculos.formatearMoneda(mora)} por mora de $diasMora días)."
                    } else {
                        "El vehículo ${alquiler.autoMarca} ${alquiler.autoModelo} fue recibido a tiempo y ya está disponible en el catálogo."
                    }
                    dialogoExitoVisible = true
                }
                alquilerParaRecibir = null
            }
        )
    }

    DialogoMensaje(
        visible = dialogoExitoVisible,
        tipo = TipoMensaje.EXITO,
        titulo = tituloExito,
        mensaje = mensajeExito,
        textoBoton = "ACEPTAR",
        onDismiss = { dialogoExitoVisible = false }
    )
}

enum class TipoEstadoActivo { VENCIDO, HOY, VIGENTE }

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
            .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
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
                            text = if (esParaHoy) "RECOGE HOY" else "PROGRAMADA",
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

@Composable
fun AlquilerActivoCard(
    alquiler: Alquiler,
    tipoEstado: TipoEstadoActivo,
    onRecibirAuto: () -> Unit
) {
    val diasMora = remember(alquiler.fechaEntrega) { Calculos.calcularDiasMora(alquiler.fechaEntrega) }
    val montoMora = remember(diasMora) { Calculos.calcularMontoMora(diasMora) }

    val borderColor = when (tipoEstado) {
        TipoEstadoActivo.VENCIDO -> Color(0xFFFF5252)
        TipoEstadoActivo.HOY -> StatusOrangeGlow
        TipoEstadoActivo.VIGENTE -> BorderSubtle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    when (tipoEstado) {
                        TipoEstadoActivo.VENCIDO -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VENCIDO ($diasMora DÍA${if (diasMora > 1) "S" else ""} ATRASO)",
                                    color = Color(0xFFFF5252),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        TipoEstadoActivo.HOY -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusOrangeGlow.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("RETORNO HOY", color = StatusOrangeGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        TipoEstadoActivo.VIGENTE -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreenGlow.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("EN REGLA", color = StatusGreenGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusRedGlow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("EN USO", color = StatusRedGlow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Cliente: ${alquiler.nombreCliente}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Teléfono: ${alquiler.telefonoCliente}", color = TextSecondary, fontSize = 12.sp)
            Text(
                text = "Fecha Pactada: ${alquiler.fechaEntregaTexto}",
                color = if (tipoEstado == TipoEstadoActivo.VENCIDO) Color(0xFFFF5252) else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            if (tipoEstado == TipoEstadoActivo.VENCIDO) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF5252).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mora acumulada ($diasMora días):", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("+${Calculos.formatearMoneda(montoMora)}", color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRecibirAuto,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoEstado == TipoEstadoActivo.VENCIDO) Color(0xFFFF5252) else StatusGreenGlow
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (tipoEstado == TipoEstadoActivo.VENCIDO) "LIQUIDAR Y RECIBIR VEHÍCULO" else "RECIBIR VEHÍCULO (MARCAR DISPONIBLE)",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DialogoChecklistEntrega(
    alquiler: Alquiler,
    onDismiss: () -> Unit,
    onConfirmarEntrega: () -> Unit
) {
    var checkDocumentos by remember { mutableStateOf(false) }
    var checkContrato by remember { mutableStateOf(false) }
    var checkPago by remember { mutableStateOf(false) }

    val todosListos = checkDocumentos && checkContrato && checkPago

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    text = "Validación de Entrega",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${alquiler.autoMarca} ${alquiler.autoModelo} • ${alquiler.nombreCliente}",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Verifica los requisitos obligatorios antes de entregar las llaves del vehículo:",
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
                        Text("DUI y Licencia vigentes verificados", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = checkDocumentos,
                        onCheckedChange = { checkDocumentos = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StatusGreenGlow,
                            uncheckedTrackColor = DarkBackground
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contrato firmado", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Firma del contrato de arrendamiento", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = checkContrato,
                        onCheckedChange = { checkContrato = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StatusGreenGlow,
                            uncheckedTrackColor = DarkBackground
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pago o depósito", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Monto: ${Calculos.formatearMoneda(alquiler.costoTotal)} recibido", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = checkPago,
                        onCheckedChange = { checkPago = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StatusGreenGlow,
                            uncheckedTrackColor = DarkBackground
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmarEntrega,
                enabled = todosListos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreenGlow,
                    disabledContainerColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CONFIRMAR Y ENTREGAR",
                    color = if (todosListos) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = TextSecondary, fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun DialogoRecepcionVehiculo(
    alquiler: Alquiler,
    onDismiss: () -> Unit,
    onConfirmarRecepcion: () -> Unit
) {
    val diasMora = remember(alquiler.fechaEntrega) { Calculos.calcularDiasMora(alquiler.fechaEntrega) }
    val montoMora = remember(diasMora) { Calculos.calcularMontoMora(diasMora) }
    val totalFinal = alquiler.costoTotal + montoMora

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (diasMora > 0) "Liquidación por Devolución Tardía" else "Recepción del Vehículo",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${alquiler.autoMarca} ${alquiler.autoModelo} • ${alquiler.nombreCliente}",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Costo Alquiler Base:", color = TextSecondary, fontSize = 12.sp)
                    Text(Calculos.formatearMoneda(alquiler.costoTotal), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (diasMora > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Recargo por Mora ($diasMora días):", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("+${Calculos.formatearMoneda(montoMora)}", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL FINAL LIQUIDADO:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = Calculos.formatearMoneda(totalFinal),
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Text(
                        text = "El vehículo fue devuelto dentro del plazo acordado. No aplica recargo.",
                        color = StatusGreenGlow,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmarRecepcion,
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreenGlow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("FINALIZAR Y LIBERAR AUTO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = TextSecondary)
            }
        }
    )
}