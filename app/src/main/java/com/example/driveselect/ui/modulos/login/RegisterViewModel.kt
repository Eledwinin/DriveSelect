package com.example.driveselect.ui.modulos.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var nombre by mutableStateOf("")
        private set

    var correo by mutableStateOf("")
        private set

    var clave by mutableStateOf("")
        private set

    var confirmarClave by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    fun onNombreChange(nuevoNombre: String) {
        nombre = nuevoNombre
        mensajeError = null
    }

    fun onCorreoChange(nuevoCorreo: String) {
        correo = nuevoCorreo
        mensajeError = null
    }

    fun onClaveChange(nuevaClave: String) {
        clave = nuevaClave
        mensajeError = null
    }

    fun onConfirmarClaveChange(nuevaClave: String) {
        confirmarClave = nuevaClave
        mensajeError = null
    }

    fun registrarUsuario(onExito: () -> Unit, onError: () -> Unit) {
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()

        if (nombreLimpio.isBlank() || correoLimpio.isBlank() || clave.isBlank()) {
            mensajeError = "Por favor, completa todos los campos"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            mensajeError = "Ingresa un correo electrónico válido"
            return
        }

        if (clave.length < 6) {
            mensajeError = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (clave != confirmarClave) {
            mensajeError = "Las contraseñas no coinciden"
            return
        }

        if (!esContrasenaValida(clave)) {
            mensajeError = "La contraseña debe tener al menos una letra mayúscula, un número y un carácter especial"
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeError = null

            try {
                val authResult = auth.createUserWithEmailAndPassword(correoLimpio, clave).await()
                val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID")

                // Crear un nuevo documento en la colección "usuarios"
                val nuevoUsuario = mapOf(
                    "id" to uid,
                    "nombre" to nombreLimpio,
                    "correo" to correoLimpio,
                    "telefono" to "",
                    "dui" to "",
                    "licencia" to "",
                    "rol" to "cliente"
                )

                FirebaseService.db.collection("usuarios")
                    .document(uid)
                    .set(nuevoUsuario)
                    .await()

                isLoading = false
                onExito()
            } catch (e: Exception) {
                isLoading = false
                mensajeError = e.localizedMessage ?: "Error al registrar usuario"
                onError()
            }
        }
    }

    fun esContrasenaValida(contrasena: String): Boolean {
        val patron = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!._?*\\-]).{8,}$")
        return patron.matches(contrasena)
    }
}