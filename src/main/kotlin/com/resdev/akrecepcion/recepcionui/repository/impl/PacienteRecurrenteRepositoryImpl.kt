package com.resdev.akrecepcion.recepcionui.repository.impl

import com.resdev.akrecepcion.recepcionui.dao.ConsultaExternaDao
import com.resdev.akrecepcion.recepcionui.dao.PacienteRecurrenteDao
import com.resdev.akrecepcion.recepcionui.repository.PacienteRecurrenteCard
import com.resdev.akrecepcion.recepcionui.repository.PacienteRecurrenteRepository

class PacienteRecurrenteRepositoryImpl(
    private val pacienteDao: PacienteRecurrenteDao,
    private val consultaDao: ConsultaExternaDao,
) : PacienteRecurrenteRepository {
    override fun latestCards(limit: Int): List<PacienteRecurrenteCard> =
        pacienteDao.latest(limit = limit).map { p ->
            val c = consultaDao.latestResumenByCodigoUsuario(p.codigoUsuario)
            PacienteRecurrenteCard(
                codigoUsuario = p.codigoUsuario,
                dpi = p.dpi?.trim().orEmpty(),
                nombre = p.nombre?.trim().orEmpty(),
                fechaNacimiento = p.fechaNacimiento,
                programa = p.programa,
                telefono = p.telefono,
                email = p.email,
                direccion = p.direccion,
                fechaRegistro = p.fechaRegistro,
                imc = c?.imc,
                historiaEnfermedad = c?.historiaEnfermedad,
            )
        }

    override fun searchCards(query: String, limit: Int): List<PacienteRecurrenteCard> =
        pacienteDao.search(query = query, limit = limit).map { p ->
            val c = consultaDao.latestResumenByCodigoUsuario(p.codigoUsuario)
            PacienteRecurrenteCard(
                codigoUsuario = p.codigoUsuario,
                dpi = p.dpi?.trim().orEmpty(),
                nombre = p.nombre?.trim().orEmpty(),
                fechaNacimiento = p.fechaNacimiento,
                programa = p.programa,
                telefono = p.telefono,
                email = p.email,
                direccion = p.direccion,
                fechaRegistro = p.fechaRegistro,
                imc = c?.imc,
                historiaEnfermedad = c?.historiaEnfermedad,
            )
        }
}

