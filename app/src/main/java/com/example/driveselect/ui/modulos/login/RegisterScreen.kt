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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driveselect.ui.theme.*

@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }

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

            // Header compacto
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

            Spacer(modifier = Modifier.height(16.dp))

            // Card principal con tema oscuro
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
                        text = "Crear nueva cuenta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Registro de cliente",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // NOMBRE COMPLETO
                    Text(
                        text = "NOMBRE COMPLETO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej. Carlos Mendoza", color = TextSecondary.copy(alpha = 0.5f)) },
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
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // CONTRASEÑA
                    Text(
                        text = "CONTRASEÑA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.clave,
                        onValueChange = { viewModel.onClaveChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mínimo 8 caracteres", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
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
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // CONFIRMAR CONTRASEÑA
                    Text(
                        text = "CONFIRMAR CONTRASEÑA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.confirmarClave,
                        onValueChange = { viewModel.onConfirmarClaveChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Repite tu contraseña", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            keyboardType = KeyboardType.Password,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                            )
                            .clickable(enabled = !viewModel.isLoading) {
                                viewModel.registrarUsuario(
                                    onExito = {
                                        Toast.makeText(context, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                                        onRegistroExitoso()
                                    },
                                    onError = {
                                        Toast.makeText(context, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
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
                                text = "CREAR CUENTA",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "¿Ya tienes cuenta? ",
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