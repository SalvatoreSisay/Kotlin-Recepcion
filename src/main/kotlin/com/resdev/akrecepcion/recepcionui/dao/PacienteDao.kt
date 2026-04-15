package com.resdev.akrecepcion.recepcionui.dao

import java.time.LocalDate

/**
 * Payload minimo para crear un paciente en `01pacientesce`.
 *
 * Nota: `edad` no existe en la tabla y se calcula desde `fechaNacimiento` solo para UI.
 */
data class PacienteNuevo(
    val nombre: String,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val dpi: String = "",
    val telefono: String = "",
    val etnia: String = "",
    val estadoCivil: String = "",
    val ocupacion: String = "",
    val direccion: String,
    val pais: String,
    val departamento: String = "",
    val municipio: String,
    val canton: String,
    val direccionAviso: String = "",
    val lugarNacimiento: String = "",
    val programa: String = "",
    val igss: String = "",
    val pacienteAltoRiesgo: String = "",
    val observaciones: String = "",
    val observa1: String = "",
    val observa2: String = "",
)

interface PacienteDao {
    /**
     * Crea el registro y retorna el `codigousuario` generado.
     */
    fun insert(paciente: PacienteNuevo, quienRegistra: String? = null): Int
}
