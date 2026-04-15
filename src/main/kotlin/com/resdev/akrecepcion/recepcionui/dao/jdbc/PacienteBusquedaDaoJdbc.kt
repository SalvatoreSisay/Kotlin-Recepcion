package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.PacienteBusquedaDao
import com.resdev.akrecepcion.recepcionui.dao.PacienteSearchPage
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider
import com.resdev.akrecepcion.recepcionui.model.Paciente
import java.time.LocalDate

class PacienteBusquedaDaoJdbc : PacienteBusquedaDao {
    override fun search(query: String?, desde: LocalDate?, limit: Int, offset: Int): PacienteSearchPage {
        val q = query?.trim().orEmpty()
        val ds = DataSourceProvider.getRequired()

        val where = StringBuilder(" WHERE 1=1")
        val params = ArrayList<(java.sql.PreparedStatement, Int) -> Unit>()

        if (q.isNotBlank()) {
            // Búsqueda simple: nombre / dpi / telefono
            where.append(" AND (`nombre` LIKE ? OR `dpi` LIKE ? OR `telefono` LIKE ?)")
            val like = "%$q%"
            params.add { ps, idx -> ps.setString(idx, like) }
            params.add { ps, idx -> ps.setString(idx, like) }
            params.add { ps, idx -> ps.setString(idx, like) }
        }

        if (desde != null) {
            where.append(" AND (`fecharegistro` IS NOT NULL AND `fecharegistro` >= ?)")
            params.add { ps, idx -> ps.setDate(idx, java.sql.Date.valueOf(desde)) }
        }

        val countSql = "SELECT COUNT(*) AS c FROM `01pacientesce`${where}"
        val pageSql =
            """
            SELECT `dpi`, `nombre`, `fechanacimiento`, `sexo`, `telefono`, `fecharegistro`
            FROM `01pacientesce`
            ${where}
            ORDER BY `fecharegistro` DESC, `codigousuario` DESC
            LIMIT ? OFFSET ?
            """.trimIndent()

        ds.connection.use { conn ->
            val total =
                conn.prepareStatement(countSql).use { ps ->
                    bindParams(ps, params)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getInt(1) else 0
                    }
                }

            val items =
                conn.prepareStatement(pageSql).use { ps ->
                    val nextIdx = bindParams(ps, params)
                    ps.setInt(nextIdx, limit.coerceAtLeast(1))
                    ps.setInt(nextIdx + 1, offset.coerceAtLeast(0))
                    ps.executeQuery().use { rs ->
                        val out = ArrayList<Paciente>(minOf(limit, 100))
                        while (rs.next()) {
                            out.add(
                                Paciente(
                                    dpi = rs.getString("dpi"),
                                    nombre = rs.getString("nombre"),
                                    fechaNacimiento = rs.getDate("fechanacimiento")?.toLocalDate(),
                                    sexo = rs.getString("sexo"),
                                    telefono = rs.getString("telefono"),
                                    fechaRegistro = rs.getDate("fecharegistro")?.toLocalDate(),
                                ),
                            )
                        }
                        out
                    }
                }

            return PacienteSearchPage(items = items, total = total)
        }
    }

    private fun bindParams(
        ps: java.sql.PreparedStatement,
        params: List<(java.sql.PreparedStatement, Int) -> Unit>,
    ): Int {
        var idx = 1
        params.forEach { binder ->
            binder(ps, idx)
            idx++
        }
        return idx
    }
}
