package com.example.driveselect.ui.modulos.gestion

import androidx.lifecycle.ViewModel
import com.example.driveselect.data.repository.AutoRepository

class GestionViewModel(
    private val repository: AutoRepository = AutoRepository()
) : ViewModel() {

    fun entregarVehiculo(autoId: String, onExito: () -> Unit = {}) {
        // Se conectará más adelante
    }

    fun devolverVehiculo(autoId: String, onExito: () -> Unit = {}) {
        // Se conectará más adelante
    }
}