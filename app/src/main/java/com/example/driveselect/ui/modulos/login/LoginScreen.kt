package com.example.driveselect.ui.modulos.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveselect.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onOlvideClaveClick: () -> Unit,
    onIrARegistro: () -> Unit
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            account?.idToken?.let { idToken ->
                viewModel.iniciarSesionConGoogle(idToken, onLoginExitoso)
            }
        } catch (e: Exception) {
            // Manejo de cancelación/error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DRIVE SELECT",
            color = GoldPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Ingresa a tu cuenta para continuar",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico", color = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña", color = TextSecondary) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error, color = StatusRedGlow, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onOlvideClaveClick() }
            )
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error, color = StatusRedGlow, fontSize = 12.sp)
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
                .clickable(enabled = !isLoading) {
                    viewModel.iniciarSesion(correo, contrasena, onLoginExitoso)
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "INICIAR SESIÓN",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
            Text(" O ", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("404327744811-1acekkr9nu9n098ht7ahc5lv11jcoruh.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continuar con Google", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "¿No tienes cuenta? ", color = TextSecondary, fontSize = 13.sp)
            Text(
                text = "Regístrate",
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onIrARegistro() }
            )
        }
    }
}