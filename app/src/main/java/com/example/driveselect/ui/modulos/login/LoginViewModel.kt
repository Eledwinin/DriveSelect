package com.example.driveselect.ui.modulos.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.driveselect.data.firebase.FirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun iniciarSesion(correo: String, contrasena: String, onExito: () -> Unit) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _errorMessage.value = "Por favor completa todos los campos"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(correo.trim(), contrasena)
            .addOnSuccessListener {
                _isLoading.value = false
                onExito()
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al iniciar sesión: ${exception.localizedMessage}"
            }
    }

    fun iniciarSesionConGoogle(idToken: String, onExito: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val userRef = FirebaseService.db.collection("usuarios").document(user.uid)
                    userRef.get().addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            val nuevoUsuario = mapOf(
                                "id" to user.uid,
                                "nombre" to (user.displayName ?: ""),
                                "correo" to (user.email ?: ""),
                                "telefono" to "",
                                "dui" to "",
                                "licencia" to "",
                                "rol" to "cliente"
                            )
                            userRef.set(nuevoUsuario).addOnCompleteListener {
                                _isLoading.value = false
                                onExito()
                            }
                        } else {
                            _isLoading.value = false
                            onExito()
                        }
                    }.addOnFailureListener {
                        _isLoading.value = false
                        onExito()
                    }
                } else {
                    _isLoading.value = false
                    onExito()
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error con Google: ${exception.localizedMessage}"
            }
    }

    fun cerrarSesion(context: Context, onCompleto: () -> Unit) {
        try {
            // cierra sesion en firebase
            auth.signOut()

            // esto desvincula la cuenta de google actual
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {

                _errorMessage.value = null
                _isLoading.value = false
                onCompleto()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage}"
        }
    }
}

