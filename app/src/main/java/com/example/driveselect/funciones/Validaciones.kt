package com.example.driveselect.funciones

object Validaciones {

    // Comprueba que el correo tenga formato real (ejemplo@dominio.com)
    fun esCorreoValido(correo: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()
    }

    // Teléfono en El Salvador: exactamente 8 números
    fun esTelefonoValido(telefono: String): Boolean {
        val limpio = telefono.trim()
        return limpio.length == 8 && limpio.all { it.isDigit() }
    }

    // DUI / Pasaporte no vacío y con longitud mínima
    fun esDocumentoValido(documento: String): Boolean {
        return documento.trim().length >= 8
    }

    // Nombre no vacío y con más de 2 caracteres
    fun esNombreValido(nombre: String): Boolean {
        return nombre.trim().length >= 3
    }
}