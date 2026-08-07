package com.example.driveselect.data.repository

import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.model.AutoEstado
import com.google.firebase.firestore.ListenerRegistration

class AutoRepository {

    // inicializar la bd
    fun iniciarInventario(onComplete: () -> Unit) {
        FirebaseService.db.collection("autos").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val autosIniciales = listOf(
                        Auto("", "Toyota", "Corolla 2023", 35.0, AutoEstado.DISPONIBLE.displayName, "corolla20233"),
                        Auto("", "Honda", "Civic 2024", 40.0, AutoEstado.DISPONIBLE.displayName, "civic2024"),
                        Auto("", "Nissan", "Sentra 2023", 32.0, AutoEstado.DISPONIBLE.displayName, "centra2023"),
                        Auto("", "Hyundai", "Tucson 2023", 50.0, AutoEstado.DISPONIBLE.displayName, "tucson2023"),
                        Auto("", "Kia", "Sportage 2023", 55.0, AutoEstado.DISPONIBLE.displayName, "sportage2023"),
                        Auto("", "Mazda", "CX-5 2023", 52.0, AutoEstado.DISPONIBLE.displayName, "cx5"),
                        Auto("", "Ford", "Mustang 2022", 85.0, AutoEstado.DISPONIBLE.displayName, "mustang2022"),
                        Auto("", "Chevrolet", "Camaro 2023", 95.0, AutoEstado.DISPONIBLE.displayName, "camaro2023")
                    )

                    val batch = FirebaseService.db.batch()
                    autosIniciales.forEach { auto ->
                        val docRef = FirebaseService.db.collection("autos").document()
                        val autoconId = auto.copy(id = docRef.id)
                        batch.set(docRef, autoconId)
                    }
                    batch.commit().addOnSuccessListener { onComplete() }
                        .addOnFailureListener { onComplete() }
                } else {
                    onComplete()
                }
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    // funcion para obtener los carros de la bd
    fun obtenerAutos(onResultado: (List<Auto>) -> Unit): ListenerRegistration {
        return FirebaseService.db.collection("autos").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onResultado(emptyList())
                return@addSnapshotListener
            }

            val lista = snapshot.documents.mapNotNull { doc ->
                try {
                    Auto(
                        id = doc.id,
                        marca = doc.getString("marca") ?: "",
                        modelo = doc.getString("modelo") ?: "",
                        precioPorDia = doc.getDouble("precioPorDia") ?: 0.0,
                        estado = doc.getString("estado") ?: AutoEstado.DISPONIBLE.displayName,
                        imagenUrl = doc.getString("imagenUrl") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onResultado(lista)
        }
    }

    // cambiar el estado del auto en firestore
    fun actualizarEstadoAuto(autoId: String, nuevoEstado: AutoEstado, onSuccess: () -> Unit) {
        if (autoId.isEmpty()) return
        FirebaseService.db.collection("autos").document(autoId)
            .update("estado", nuevoEstado.displayName)
            .addOnSuccessListener { onSuccess() }
    }

    // Registro del alquiler
    fun registrarAlquiler(alquiler: Alquiler, onSuccess: () -> Unit) {
        val docRef = FirebaseService.db.collection("alquileres").document()
        val alquilerconId = alquiler.copy(id = docRef.id)

        docRef.set(alquilerconId).addOnSuccessListener {
            actualizarEstadoAuto(alquiler.autoId, AutoEstado.ALQUILADO_EN_PROCESO, onSuccess)
        }
    }

    fun marcarComoRetirado(autoId: String, onSuccess: () -> Unit) {
        actualizarEstadoAuto(autoId, AutoEstado.ALQUILADO_EN_USO, onSuccess)
    }

    fun marcarComoDevuelto(autoId: String, onSuccess: () -> Unit) {
        actualizarEstadoAuto(autoId, AutoEstado.DISPONIBLE, onSuccess)
    }
}