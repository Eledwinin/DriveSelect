package com.example.driveselect.ui.navigation

import androidx.lifecycle.ViewModel
import com.example.driveselect.data.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val _rol = MutableStateFlow<String?>("cliente")
    val rol: StateFlow<String?> = _rol.asStateFlow()

    init {
        obtenerRolUsuario()
    }

    fun obtenerRolUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _rol.value = "cliente"
            return
        }

        FirebaseService.db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rolGuardado = document.getString("rol") ?: "cliente"
                    _rol.value = rolGuardado.lowercase()
                } else {
                    _rol.value = "cliente"
                }
            }
            .addOnFailureListener {
                _rol.value = "cliente"
            }
    }
}