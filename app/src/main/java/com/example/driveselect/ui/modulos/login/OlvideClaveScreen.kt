package com.example.driveselect.ui.modulos.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driveselect.ui.theme.*

@Composable
fun OlvideClaveScreen(
    onCorreoEnviadoExito: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OlvideClaveViewModel = viewModel()
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de regresar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = TextPrimary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "Logo",
                tint = GoldPrimary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "DRIVE SELECT",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = GoldPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Recuperar Contraseña",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Ingresa tu correo electrónico registrado y te enviaremos las instrucciones para restablecer tu contraseña.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // CORREO ELECTRÓNICO
                    Text(
                        text = "CORREO ELECTRÓNICO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.correo,
                        onValueChange = { viewModel.onCorreoChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("correo@ejemplo.com", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )

                    viewModel.mensajeError?.let { error ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = error,
                            color = StatusRedGlow,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                            )
                            .clickable(enabled = !viewModel.isLoading) {
                                viewModel.enviarCorreoRecuperacion(
                                    onExito = {
                                        Toast.makeText(
                                            context,
                                            "Correo enviado con éxito. Revisa tu bandeja de entrada.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onCorreoEnviadoExito()
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            "Error al enviar el correo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "ENVIAR INSTRUCCIONES",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Volver a ",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Iniciar Sesión",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateBack() }
                        )
                    }
                }
            }
        }
    }
}