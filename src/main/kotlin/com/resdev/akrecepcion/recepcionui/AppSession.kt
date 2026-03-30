package com.resdev.akrecepcion.recepcionui

import java.time.LocalDateTime

/**
 * Estado mínimo en memoria para demo UI.
 * No hay backend, así que esto funciona como "source of truth" local para Perfil/Saludo.
 */
object AppSession {
    data class UserProfile(
        var nombreCompleto: String,
        var usuario: String,
        var rol: String,
        var departamento: String,
        var correo: String,
        var telefono: String,
        var notificaciones: Boolean,
        var estado: String = "ACTIVO",
        var lastUpdated: LocalDateTime = LocalDateTime.now(),
    )

    var currentUser: UserProfile =
        UserProfile(
            nombreCompleto = "Juliane Castillo",
            usuario = "admin",
            rol = "Recepción",
            departamento = "Admisiones",
            correo = "juliane.castillo@hospitalito.gt",
            telefono = "+502 5555 5555",
            notificaciones = true,
        )

    // Demo: credencial en memoria para cambio de contraseña dentro de esta misma ejecución.
    var password: String = "1234"
        private set

    fun updatePassword(newPassword: String) {
        password = newPassword
    }
}

