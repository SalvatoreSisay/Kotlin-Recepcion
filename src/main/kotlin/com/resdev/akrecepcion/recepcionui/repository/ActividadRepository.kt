package com.resdev.akrecepcion.recepcionui.repository

data class ActividadRow(
    val nombre: String,
    val codigoUsuario: Int,
    val tipo: String,
    val estado: String,
    val hora: String,
)

interface ActividadRepository {
    fun latestActividad(limit: Int): List<ActividadRow>
}

