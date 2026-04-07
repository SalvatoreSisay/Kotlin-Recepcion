package com.resdev.akrecepcion.recepcionui.dao

import com.resdev.akrecepcion.recepcionui.model.Usuario

data class UsuarioRecord(
    val usuario: Usuario,
    val password: String,
)

interface UsuarioDao {
    fun findByUsuario(usuario: String): UsuarioRecord?
}

