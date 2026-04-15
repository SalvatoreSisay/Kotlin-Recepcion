package com.resdev.akrecepcion.recepcionui.dao

import com.resdev.akrecepcion.recepcionui.model.Paciente
import java.time.LocalDate

data class PacienteSearchPage(
    val items: List<Paciente>,
    val total: Int,
)

interface PacienteBusquedaDao {
    fun search(
        query: String?,
        desde: LocalDate?,
        limit: Int,
        offset: Int,
    ): PacienteSearchPage
}

