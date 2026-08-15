package com.example.driveselect.ui.modulos.historial

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistorialViewModel : ViewModel() {

    private val _historial = MutableStateFlow<List<Alquiler>>(emptyList())
    val historial: StateFlow<List<Alquiler>> = _historial.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarHistorialUsuario()
    }

    fun cargarHistorialUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        _isLoading.value = true

        FirebaseService.db.collection("alquileres")
            .whereEqualTo("usuarioId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HISTORIAL_DEBUG", "Error al leer de Firestore: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaLimpia = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Alquiler::class.java)?.copy(id = doc.id)
                    }.sortedByDescending { it.fechaRecogida }

                    _historial.value = listaLimpia
                }
                _isLoading.value = false
            }
    }
}