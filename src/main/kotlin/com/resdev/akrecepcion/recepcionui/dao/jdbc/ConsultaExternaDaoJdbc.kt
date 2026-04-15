package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.ConsultaExternaDao
import com.resdev.akrecepcion.recepcionui.dao.ConsultaExternaResumen
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider

class ConsultaExternaDaoJdbc : ConsultaExternaDao {
    override fun latestResumenByCodigoUsuario(codigoUsuario: Int): ConsultaExternaResumen? {
        val ds = DataSourceProvider.getRequired()
        val sql =
            """
            SELECT `fechaconsulta`, `imc`, `historiaenfermedad`
            FROM `50consultaexterna`
            WHERE `codigousuario` = ?
            ORDER BY `fechaconsulta` DESC, `idconsultas` DESC
            LIMIT 1
            """.trimIndent()

        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setInt(1, codigoUsuario)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    val fecha = rs.getTimestamp("fechaconsulta").toLocalDateTime()
                    val imc = rs.getInt("imc").let { if (rs.wasNull()) null else it }
                    val historia = rs.getString("historiaenfermedad")

                    return ConsultaExternaResumen(
                        fechaConsulta = fecha,
                        imc = imc,
                        historiaEnfermedad = historia,
                    )
                }
            }
        }
    }
}

