package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.ActividadPacienteDao
import com.resdev.akrecepcion.recepcionui.dao.ActividadPacienteDbRow
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider

class ActividadPacienteDaoJdbc : ActividadPacienteDao {
    override fun latest(limit: Int): List<ActividadPacienteDbRow> {
        val ds = DataSourceProvider.getRequired()
        val sql =
            """
            SELECT `codigousuario`, `nombre`, `fecharegistro`, `fecharegistro2`
            FROM `01pacientesce`
            ORDER BY (`fecharegistro2` IS NULL) ASC, `fecharegistro2` DESC, `codigousuario` DESC
            LIMIT ?
            """.trimIndent()

        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setInt(1, limit.coerceAtLeast(1))
                ps.executeQuery().use { rs ->
                    val out = ArrayList<ActividadPacienteDbRow>(limit.coerceAtMost(20))
                    while (rs.next()) {
                        out.add(
                            ActividadPacienteDbRow(
                                codigoUsuario = rs.getInt("codigousuario"),
                                nombre = rs.getString("nombre"),
                                fechaRegistro = rs.getDate("fecharegistro")?.toLocalDate(),
                                fechaRegistroTs = rs.getTimestamp("fecharegistro2")?.toLocalDateTime(),
                            ),
                        )
                    }
                    return out
                }
            }
        }
    }
}

