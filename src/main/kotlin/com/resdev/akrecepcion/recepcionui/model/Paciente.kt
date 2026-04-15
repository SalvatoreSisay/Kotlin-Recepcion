package com.resdev.akrecepcion.recepcionui.model

import java.time.LocalDate

/**
 * Modelo mínimo usado en vistas de búsqueda/listado.
 */
data class Paciente(
    val dpi: String?,
    val nombre: String?,
    val fechaNacimiento: LocalDate?,
    val sexo: String?,
    val telefono: String?,
    val fechaRegistro: LocalDate?,
)

