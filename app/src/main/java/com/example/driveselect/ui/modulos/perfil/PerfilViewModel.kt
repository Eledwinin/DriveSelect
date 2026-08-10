package com.example.driveselect.ui.modulos.perfil

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveselect.data.firebase.FirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PerfilViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var nombre by mutableStateOf("")
        private set

    var correo by mutableStateOf("")
        private set

    var rol by mutableStateOf("")
        private set

    var telefono by mutableStateOf("")
        private set

    var dui by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        isLoading = true

        FirebaseService.db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombre = doc.getString("nombre") ?: "Sin nombre"
                    correo = doc.getString("correo") ?: (auth.currentUser?.email ?: "")
                    rol = doc.getString("rol") ?: "cliente"
                    telefono = doc.getString("telefono") ?: ""
                    dui = doc.getString("dui") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun cerrarSesion(context: Context, onExito: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                onExito()
            }
        }
    }

    fun actualizarDatos(nuevoTelefono: String, nuevoDui: String, onExito: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        isLoading = true

        val updates = mapOf(
            "telefono" to nuevoTelefono.trim(),
            "dui" to nuevoDui.trim()
        )

        FirebaseService.db.collection("usuarios").document(uid).update(updates)
            .addOnSuccessListener {
                telefono = nuevoTelefono.trim()
                dui = nuevoDui.trim()
                isLoading = false
                onExito()
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
}