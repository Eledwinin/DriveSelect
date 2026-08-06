package com.example.driveselect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.driveselect.data.firebase.FirebaseService
import com.example.driveselect.ui.theme.DriveSelectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        probarFirestore()

        setContent {
            DriveSelectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "Probando conexión a Firestore...",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun probarFirestore() {
        val testData = hashMapOf(
            "mensaje" to "¡Conectado desde DriveSelect",
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseService.db.collection("prueba_conexion")
            .add(testData)
            .addOnSuccessListener { doc ->
                Log.d("FIREBASE_TEST", "✅ ¡CONECTADO! ID del doc: ${doc.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_TEST", "❌ ERROR al conectar con Firestore", e)
            }
    }
}