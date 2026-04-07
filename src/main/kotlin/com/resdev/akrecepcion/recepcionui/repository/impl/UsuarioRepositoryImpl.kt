package com.resdev.akrecepcion.recepcionui.repository.impl

import com.resdev.akrecepcion.recepcionui.dao.UsuarioDao
import com.resdev.akrecepcion.recepcionui.repository.AuthResult
import com.resdev.akrecepcion.recepcionui.repository.UsuarioRepository

class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao,
) : UsuarioRepository {
    override fun authenticate(usuario: String, password: String): AuthResult {
        val record = usuarioDao.findByUsuario(usuario) ?: return AuthResult.InvalidCredentials

        // Hoy la tabla guarda pass como varchar(25). Lo comparamos directo.
        // Si luego migramos a hash (bcrypt/argon2), este punto cambia para comparar hashes.
        return if (record.password == password) AuthResult.Success(record.usuario) else AuthResult.InvalidCredentials
    }
}

