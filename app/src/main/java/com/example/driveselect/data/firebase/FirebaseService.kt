package com.example.driveselect.data.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object FirebaseService {
    val db by lazy {
        Firebase.firestore
    }

    val autosRef: CollectionReference
        get() = db.collection("autos")

    val alquileresRef: CollectionReference
        get() = db.collection("alquileres")
}