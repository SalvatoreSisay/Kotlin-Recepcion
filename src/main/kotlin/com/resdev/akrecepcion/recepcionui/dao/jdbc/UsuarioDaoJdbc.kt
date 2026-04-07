package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.UsuarioDao
import com.resdev.akrecepcion.recepcionui.dao.UsuarioRecord
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider
import com.resdev.akrecepcion.recepcionui.model.Usuario

class UsuarioDaoJdbc : UsuarioDao {
    override fun findByUsuario(usuario: String): UsuarioRecord? {
        val ds = DataSourceProvider.getRequired()
        ds.connection.use { conn ->
            conn.prepareStatement(
                """
                SELECT id_usuario, usuario, pass, id_nivel, nombre_usuario, foto
                FROM `90usuarios`
                WHERE usuario = ?
                LIMIT 1
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, usuario)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    val idUsuario = rs.getInt("id_usuario")
                    val user = rs.getString("usuario")
                    val pass = rs.getString("pass") ?: ""
                    val idNivel = rs.getInt("id_nivel")
                    val nombreUsuario = rs.getString("nombre_usuario") ?: user

                    val fotoBytes =
                        rs.getBytes("foto")?.takeIf { it.isNotEmpty() }

                    return UsuarioRecord(
                        usuario =
                            Usuario(
                                idUsuario = idUsuario,
                                usuario = user,
                                nombreUsuario = nombreUsuario,
                                idNivel = idNivel,
                                foto = fotoBytes,
                            ),
                        password = pass,
                    )
                }
            }
        }
    }
}

