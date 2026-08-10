package com.example.driveselect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.driveselect.ui.navigation.AppNavigation
import com.example.driveselect.ui.theme.DriveSelectTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        setContent {
            DriveSelectTheme {
                AppNavigation()
            }
        }
    }
}