package com.resdev.akrecepcion.recepcionui.repository

import com.resdev.akrecepcion.recepcionui.model.Usuario

sealed class AuthResult {
    data class Success(val usuario: Usuario) : AuthResult()
    data object InvalidCredentials : AuthResult()
}

interface UsuarioRepository {
    fun authenticate(usuario: String, password: String): AuthResult
}

