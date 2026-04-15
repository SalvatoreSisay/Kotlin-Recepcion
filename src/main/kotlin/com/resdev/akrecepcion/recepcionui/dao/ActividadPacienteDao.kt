package com.resdev.akrecepcion.recepcionui.dao

import java.time.LocalDate
import java.time.LocalDateTime

data class ActividadPacienteDbRow(
    val codigoUsuario: Int,
    val nombre: String?,
    val fechaRegistro: LocalDate?,
    val fechaRegistroTs: LocalDateTime?,
)

interface ActividadPacienteDao {
    fun latest(limit: Int): List<ActividadPacienteDbRow>
}

