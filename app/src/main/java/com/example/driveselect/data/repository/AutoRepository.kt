package com.example.driveselect.data.repository

import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.data.model.Alquiler
import com.example.driveselect.data.model.Auto
import com.example.driveselect.data.model.AutoEstado
import com.google.firebase.firestore.ListenerRegistration

class AutoRepository {

    //inicializamos el inventario de los 8 vehiculos

    fun iniciarInventario(onComplete: () -> Unit) {
        FirebaseService.db.collection("autos").get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val autosIniciales = listOf(
                    Auto("", "Toyota", "Corolla 2023", 35.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Honda", "Civic 2024", 40.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Nissan", "Sentra 2023", 32.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Hyundai", "Tucson 2023", 50.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Kia", "Sportage 2023", 55.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Mazda", "CX-5 2023", 52.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Ford", "Mustang 2022", 85.00, AutoEstado.DISPONIBLE.displayName),
                    Auto("", "Chevrolet", "Camaro 2023", 90.00, AutoEstado.DISPONIBLE.displayName)
                )

                val batch = FirebaseService.db.batch()
                autosIniciales.forEach { auto ->
                    val docRef = FirebaseService.db.collection("autos").document()
                    val autoconId = auto.copy(id = docRef.id)
                    batch.set(docRef, autoconId)
                }
                batch.commit().addOnSuccessListener { onComplete() }
            } else {
                onComplete()
            }
        }
    }

    //funcion para obtener los autos de la base de datos
    fun obtenerAutos(onResultado: (List<Auto>) -> Unit): ListenerRegistration {
        return FirebaseService.db.collection("autos").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val lista = snapshot.toObjects(Auto::class.java)
                onResultado (lista)
            }
        }
    }

    // Cambiar el estado del auto en firestore
    fun actualizarEstadoAuto(autoId: String, nuevoEstado: AutoEstado, onSuccess: () -> Unit) {
        FirebaseService.db.collection("autos").document(autoId)
            .update("estado", nuevoEstado.displayName)
            .addOnSuccessListener { onSuccess() }
    }

    //esto es para el registro del alquiler, va a pasar el auto a alquilado en process

    fun registrarAlquiler(alquiler: Alquiler, onSuccess: () -> Unit){
        val docRef = FirebaseService.db.collection("alguileres").document()
        val alquilerconId = alquiler.copy (id = docRef.id)

        docRef.set (alquilerconId).addOnSuccessListener {
            actualizarEstadoAuto(alquiler.autoId, AutoEstado.ALQUILADO_EN_PROCESO, onSuccess)
        }
    }

    //cuadno el cliente retire el auto, se actualiza a "alquilado en uso"
    fun marcarComoRetirado(autoId: String, onSuccess: () -> Unit){
        actualizarEstadoAuto(autoId, AutoEstado.ALQUILADO_EN_USO, onSuccess)
    }

    //cuando el cliente regresa el auto, se actualiza a disponible
    fun marcarComoDevuelto(autoId: String, onSuccess: () -> Unit) {
        actualizarEstadoAuto(autoId, AutoEstado.DISPONIBLE, onSuccess)
    }
}


