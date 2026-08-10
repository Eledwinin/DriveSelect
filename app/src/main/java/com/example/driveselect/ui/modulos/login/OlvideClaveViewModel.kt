package com.example.driveselect.ui.modulos.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OlvideClaveViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var correo by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    fun onCorreoChange(nuevoCorreo: String) {
        correo = nuevoCorreo
        mensajeError = null
    }

    fun enviarCorreoRecuperacion(onExito: () -> Unit, onError: () -> Unit) {
        val correoLimpio = correo.trim()

        if (correoLimpio.isBlank()) {
            mensajeError = "Por favor, ingresa tu correo electrónico"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            mensajeError = "Ingresa un correo electrónico válido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeError = null

            try {
                auth.sendPasswordResetEmail(correoLimpio).await()
                isLoading = false
                onExito()
            } catch (e: Exception) {
                isLoading = false
                mensajeError = e.localizedMessage ?: "Error al enviar el correo de recuperación"
                onError()
            }
        }
    }
}