package com.resdev.akrecepcion.recepcionui.repository

import com.resdev.akrecepcion.recepcionui.model.Paciente
import java.time.LocalDate

data class PacientePage(
    val items: List<Paciente>,
    val total: Int,
)

interface PacienteRepository {
    fun search(
        query: String?,
        desde: LocalDate?,
        page: Int,
        pageSize: Int,
    ): PacientePage
}

