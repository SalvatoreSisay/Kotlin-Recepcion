package com.resdev.akrecepcion.recepcionui

import com.resdev.akrecepcion.recepcionui.dao.jdbc.UsuarioDaoJdbc
import com.resdev.akrecepcion.recepcionui.repository.impl.UsuarioRepositoryImpl
import com.resdev.akrecepcion.recepcionui.service.AuthService

/**
 * Contenedor simple de dependencias (DI manual).
 *
 * Ventajas:
 * - Un solo lugar donde se "cablea" DAO -> Repository -> Service.
 * - Los controllers quedan mas limpios.
 *
 * Nota: es minimalista a proposito (sin framework).
 */
object AppContainer {
    val usuarioDao by lazy { UsuarioDaoJdbc() }
    val usuarioRepository by lazy { UsuarioRepositoryImpl(usuarioDao) }
    val authService by lazy { AuthService(usuarioRepository) }
}

