package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.PacienteRecurrenteBase
import com.resdev.akrecepcion.recepcionui.dao.PacienteRecurrenteDao
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider

class PacienteRecurrenteDaoJdbc : PacienteRecurrenteDao {
    override fun latest(limit: Int): List<PacienteRecurrenteBase> {
        val ds = DataSourceProvider.getRequired()
        val sql =
            """
            SELECT `codigousuario`, `dpi`, `nombre`, `fechanacimiento`, `programa`, `telefono`, `email`, `DIRECCION`, `fecharegistro`
            FROM `01pacientesce`
            ORDER BY (`fecharegistro` IS NULL) ASC, `fecharegistro` DESC, `codigousuario` DESC
            LIMIT ?
            """.trimIndent()

        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setInt(1, limit.coerceAtLeast(1))
                ps.executeQuery().use { rs ->
                    val out = ArrayList<PacienteRecurrenteBase>(limit.coerceAtMost(20))
                    while (rs.next()) {
                        out.add(
                            PacienteRecurrenteBase(
                                codigoUsuario = rs.getInt("codigousuario"),
                                dpi = rs.getString("dpi"),
                                nombre = rs.getString("nombre"),
                                fechaNacimiento = rs.getDate("fechanacimiento")?.toLocalDate(),
                                programa = rs.getString("programa"),
                                telefono = rs.getString("telefono"),
                                email = rs.getString("email"),
                                direccion = rs.getString("DIRECCION"),
                                fechaRegistro = rs.getDate("fecharegistro")?.toLocalDate(),
                            ),
                        )
                    }
                    return out
                }
            }
        }
    }

    override fun search(query: String, limit: Int): List<PacienteRecurrenteBase> {
        val q = query.trim()
        val like = "%$q%"
        val codigo = q.toIntOrNull()

        val ds = DataSourceProvider.getRequired()
        val sql =
            """
            SELECT `codigousuario`, `dpi`, `nombre`, `fechanacimiento`, `programa`, `telefono`, `email`, `DIRECCION`, `fecharegistro`
            FROM `01pacientesce`
            WHERE (`nombre` LIKE ? OR `dpi` LIKE ? OR (? IS NOT NULL AND `codigousuario` = ?))
            ORDER BY (`fecharegistro` IS NULL) ASC, `fecharegistro` DESC, `codigousuario` DESC
            LIMIT ?
            """.trimIndent()

        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, like)
                ps.setString(2, like)
                if (codigo == null) ps.setNull(3, java.sql.Types.INTEGER) else ps.setInt(3, codigo)
                if (codigo == null) ps.setNull(4, java.sql.Types.INTEGER) else ps.setInt(4, codigo)
                ps.setInt(5, limit.coerceAtLeast(1))

                ps.executeQuery().use { rs ->
                    val out = ArrayList<PacienteRecurrenteBase>(limit.coerceAtMost(20))
                    while (rs.next()) {
                        out.add(
                            PacienteRecurrenteBase(
                                codigoUsuario = rs.getInt("codigousuario"),
                                dpi = rs.getString("dpi"),
                                nombre = rs.getString("nombre"),
                                fechaNacimiento = rs.getDate("fechanacimiento")?.toLocalDate(),
                                programa = rs.getString("programa"),
                                telefono = rs.getString("telefono"),
                                email = rs.getString("email"),
                                direccion = rs.getString("DIRECCION"),
                                fechaRegistro = rs.getDate("fecharegistro")?.toLocalDate(),
                            ),
                        )
                    }
                    return out
                }
            }
        }
    }
}

