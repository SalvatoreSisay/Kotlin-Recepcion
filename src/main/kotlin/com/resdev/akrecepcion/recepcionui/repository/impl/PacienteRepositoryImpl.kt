package com.resdev.akrecepcion.recepcionui.repository.impl

import com.resdev.akrecepcion.recepcionui.dao.PacienteBusquedaDao
import com.resdev.akrecepcion.recepcionui.repository.PacientePage
import com.resdev.akrecepcion.recepcionui.repository.PacienteRepository
import java.time.LocalDate

class PacienteRepositoryImpl(
    private val pacienteBusquedaDao: PacienteBusquedaDao,
) : PacienteRepository {
    override fun search(query: String?, desde: LocalDate?, page: Int, pageSize: Int): PacientePage {
        val safePage = page.coerceAtLeast(1)
        val safeSize = pageSize.coerceIn(1, 50)
        val offset = (safePage - 1) * safeSize

        val res = pacienteBusquedaDao.search(query = query, desde = desde, limit = safeSize, offset = offset)
        return PacientePage(items = res.items, total = res.total)
    }
}

