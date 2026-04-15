package com.resdev.akrecepcion.recepcionui.dao

import java.time.LocalDateTime

data class ConsultaExternaResumen(
    val fechaConsulta: LocalDateTime,
    val imc: Int?,
    val historiaEnfermedad: String?,
)

interface ConsultaExternaDao {
    fun latestResumenByCodigoUsuario(codigoUsuario: Int): ConsultaExternaResumen?
}

