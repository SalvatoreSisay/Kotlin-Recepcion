package com.resdev.akrecepcion.recepcionui.service

import com.resdev.akrecepcion.recepcionui.model.Usuario
import com.resdev.akrecepcion.recepcionui.repository.AuthResult
import com.resdev.akrecepcion.recepcionui.repository.UsuarioRepository

sealed class LoginResult {
    data class Ok(val usuario: Usuario, val rol: String, val departamento: String) : LoginResult()
    data object InvalidCredentials : LoginResult()
}

class AuthService(
    private val usuarioRepository: UsuarioRepository,
) {
    fun login(usuario: String, password: String): LoginResult {
        if (usuario.isBlank() || password.isBlank()) return LoginResult.InvalidCredentials

        return when (val res = usuarioRepository.authenticate(usuario, password)) {
            is AuthResult.Success -> {
                val (rol, depto) = mapNivel(res.usuario.idNivel)
                LoginResult.Ok(res.usuario, rol, depto)
            }
            AuthResult.InvalidCredentials -> LoginResult.InvalidCredentials
        }
    }

    private fun mapNivel(idNivel: Int): Pair<String, String> {
        // TODO: ajustar segun tu catalogo real de niveles.
        return when (idNivel) {
            1 -> "Administrador" to "Sistemas"
            2 -> "Recepción" to "Admisiones"
            3 -> "Médico" to "Consulta"
            else -> "Nivel $idNivel" to "General"
        }
    }
}

