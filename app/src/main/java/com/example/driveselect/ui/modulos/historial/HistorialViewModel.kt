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
        // Mientras no hay pantalla de Login, se usa tu ID como respaldo si currentUser es null
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "B5wrHWyadq8msloNHbSe"

        Log.d("HISTORIAL_DEBUG", "Cargando historial para UID: $uid")
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
                    val listaLimpia = snapshot.documents.map { doc ->
                        Alquiler(
                            id = doc.getString("id") ?: doc.id,
                            usuarioId = doc.getString("usuarioId") ?: "",
                            autoId = doc.getString("autoId") ?: "",
                            autoMarca = doc.getString("autoMarca") ?: "",
                            autoModelo = doc.getString("autoModelo") ?: "",
                            nombreCliente = doc.getString("nombreCliente") ?: "",
                            telefonoCliente = doc.getString("telefonoCliente") ?: "",
                            correoCliente = doc.getString("correoCliente") ?: "",
                            documentoCliente = doc.getString("documentoCliente") ?: "",
                            licenciaCliente = doc.getString("licenciaCliente") ?: "",
                            fechaRecogida = doc.getLong("fechaRecogida") ?: 0L,
                            fechaEntrega = doc.getLong("fechaEntrega") ?: 0L,
                            fechaRecogidaTexto = doc.getString("fechaRecogidaTexto") ?: "",
                            fechaEntregaTexto = doc.getString("fechaEntregaTexto") ?: "",
                            diasTotales = (doc.getLong("diasTotales") ?: 0L).toInt(),
                            costoTotal = (doc.get("costoTotal") as? Number)?.toDouble() ?: 0.0,
                            estado = doc.getString("estado") ?: "pendiente"
                        )
                    }
                    _historial.value = listaLimpia
                }
                _isLoading.value = false
            }
    }
}