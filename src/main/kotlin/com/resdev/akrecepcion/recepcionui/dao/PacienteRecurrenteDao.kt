package com.resdev.akrecepcion.recepcionui.dao

import java.time.LocalDate

data class PacienteRecurrenteBase(
    val codigoUsuario: Int,
    val dpi: String?,
    val nombre: String?,
    val fechaNacimiento: LocalDate?,
    val programa: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val fechaRegistro: LocalDate?,
)

interface PacienteRecurrenteDao {
    fun latest(limit: Int): List<PacienteRecurrenteBase>

    fun search(query: String, limit: Int): List<PacienteRecurrenteBase>
}

