package com.resdev.akrecepcion.recepcionui.repository.impl

import com.resdev.akrecepcion.recepcionui.dao.ActividadPacienteDao
import com.resdev.akrecepcion.recepcionui.repository.ActividadRepository
import com.resdev.akrecepcion.recepcionui.repository.ActividadRow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActividadRepositoryImpl(
    private val dao: ActividadPacienteDao,
) : ActividadRepository {
    private val fmtHora = DateTimeFormatter.ofPattern("HH:mm")

    override fun latestActividad(limit: Int): List<ActividadRow> {
        val now = LocalDateTime.now()
        val since = now.minusDays(1)

        return dao.latest(limit).map { r ->
            val nombre = r.nombre?.trim().orEmpty().ifBlank { "-" }
            val hora = r.fechaRegistroTs?.format(fmtHora) ?: "-"

            val isNuevo =
                r.fechaRegistroTs?.let { it.isAfter(since) } ?: run {
                    val fr = r.fechaRegistro
                    fr != null && fr >= LocalDate.now().minusDays(1)
                }

            ActividadRow(
                nombre = nombre,
                codigoUsuario = r.codigoUsuario,
                tipo = if (isNuevo) "Nuevo" else "Recurrente",
                estado = "Verificado",
                hora = hora,
            )
        }
    }
}

