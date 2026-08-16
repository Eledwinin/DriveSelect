package com.example.driveselect.ui.modulos.mensajes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.driveselect.ui.theme.*
import kotlinx.coroutines.delay

enum class TipoMensaje {
    EXITO,
    ADVERTENCIA,
    ERROR,
    INFO
}

// -------------------------------------------------------------
// 1. DIÁLOGO MODAL DE RETROALIMENTACIÓN (POP-UP)
// -------------------------------------------------------------
@Composable
fun DialogoMensaje(
    visible: Boolean,
    tipo: TipoMensaje = TipoMensaje.EXITO,
    titulo: String,
    mensaje: String,
    textoBoton: String = "ENTENDIDO",
    onDismiss: () -> Unit
) {
    if (!visible) return

    val (colorIcono, iconoTexto) = when (tipo) {
        TipoMensaje.EXITO -> Pair(StatusGreenGlow, "✓")
        TipoMensaje.ADVERTENCIA -> Pair(StatusOrangeGlow, "!")
        TipoMensaje.ERROR -> Pair(StatusRedGlow, "✕")
        TipoMensaje.INFO -> Pair(GoldPrimary, "ℹ")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colorIcono.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconoTexto,
                        color = colorIcono,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = titulo,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Text(
                text = mensaje,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (tipo) {
                        TipoMensaje.EXITO -> StatusGreenGlow
                        TipoMensaje.ERROR -> StatusRedGlow
                        TipoMensaje.ADVERTENCIA -> StatusOrangeGlow
                        TipoMensaje.INFO -> GoldPrimary
                    }
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = textoBoton,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    )
}

// -------------------------------------------------------------
// 2. BANNER TOAST FLOTANTE SUPERIOR
// -------------------------------------------------------------
@Composable
fun BannerMensajeFlotante(
    visible: Boolean,
    tipo: TipoMensaje = TipoMensaje.EXITO,
    mensaje: String,
    duracionMs: Long = 3000L,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(duracionMs)
            onDismiss()
        }
    }

    val (colorBorde, iconoTexto) = when (tipo) {
        TipoMensaje.EXITO -> Pair(StatusGreenGlow, "✓")
        TipoMensaje.ADVERTENCIA -> Pair(StatusOrangeGlow, "!")
        TipoMensaje.ERROR -> Pair(StatusRedGlow, "✕")
        TipoMensaje.INFO -> Pair(GoldPrimary, "ℹ")
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colorBorde, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(colorBorde.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iconoTexto,
                            color = colorBorde,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = mensaje,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}