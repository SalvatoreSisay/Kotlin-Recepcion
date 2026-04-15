package com.resdev.akrecepcion.recepcionui.repository

import java.time.LocalDate

data class PacienteRecurrenteCard(
    val codigoUsuario: Int,
    val dpi: String,
    val nombre: String,
    val fechaNacimiento: LocalDate?,
    val programa: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val fechaRegistro: LocalDate?,
    val imc: Int?,
    val historiaEnfermedad: String?,
)

interface PacienteRecurrenteRepository {
    fun latestCards(limit: Int): List<PacienteRecurrenteCard>

    fun searchCards(query: String, limit: Int): List<PacienteRecurrenteCard>
}

